package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static cn.zcn.json.ast.JsonCharacters.*;
import static cn.zcn.json.ast.JsonCharacters.TAB;

/**
 * Json Reader 2
 *
 * @author zicung
 */
public class JsonReader2 {

    private static final int BEGIN_OBJECT = 0x001; // {
    private static final int END_OBJECT = 0x002; // }
    private static final int BEGIN_ARRAY = 0x004;  // [
    private static final int END_ARRAY = 0x008; // ]
    private static final int OBJECT_NAME_SEPARATOR = 0x010; // :
    private static final int ELEMENT_SEPARATOR = 0x020; // ,
    private static final int OBJECT_NAME = 0x040;
    private static final int OBJECT_VALUE = 0x080;
    private static final int ARRAY_VALUE = 0x100;
    private static final Map<Integer, String> HITS = new HashMap<>();

    static {
        HITS.put(BEGIN_OBJECT, "{");
        HITS.put(END_OBJECT, "}");
        HITS.put(BEGIN_ARRAY, "[");
        HITS.put(END_ARRAY, "]");
        HITS.put(OBJECT_NAME_SEPARATOR, ":");
        HITS.put(ELEMENT_SEPARATOR, ",");
        HITS.put(OBJECT_NAME, "pair name");
        HITS.put(OBJECT_VALUE, "pair value");
        HITS.put(ARRAY_VALUE, "array element");
    }

    private final Reader reader;
    private final JsonVisitor visitor;
    private int expectedStatus;
    private int current = 0;
    private final Deque<JsonValue> valueDeque = new LinkedList<>();
    private final Deque<String> nameDeque = new LinkedList<>();

    private final char[] readBuffer = new char[12];
    private int nextPos = 0;
    private int fill = 0;

    private StringBuilder valueBuffer;
    private int valueStartPos = -1;

    private int line = 1;
    private int column = 0;

    public JsonReader2(Reader reader, JsonVisitor visitor) {
        this.reader = reader;
        this.visitor = visitor;
    }

    public JsonValue read() throws IOException {
        boolean shouldReadNext = true;
        setExpectedStatus(BEGIN_OBJECT, BEGIN_ARRAY);
        while (true) {
            if (shouldReadNext) {
                readNext();
            } else {
                shouldReadNext = true;
            }

            while (isWhitespace()) {
                readNext();
            }

            if (current == -1) {
                break;
            }

            switch (current) {
                case JSON_OBJECT_BEGIN:
                    enterObject();
                    break;
                case JSON_OBJECT_END:
                    exitObject();
                    break;
                case JSON_ARRAY_BEGIN:
                    enterArray();
                    break;
                case JSON_ARRAY_END:
                    exitArray();
                    break;
                case JSON_QUOTATION_MARK:
                    readString();
                    break;
                case JSON_VALUE_SEPARATOR:
                    enterComma();
                    break;
                case JSON_NAME_SEPARATOR:
                    enterColon();
                    break;
                case 't':
                    readTrue();
                    break;
                case 'f':
                    readFalse();
                    break;
                case 'n':
                    readNull();
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    readNumber();
                    shouldReadNext = false;
                    break;
                default:
                    throw new JsonException("Unsupported json value prefix: " + (char) current);
            }
        }

        if (valueDeque.size() > 0) {
            if (valueDeque.peek().isObject()) {
                throwUnexpectedException(JSON_OBJECT_END);
            } else {
                throwUnexpectedException(JSON_ARRAY_END);
            }
        }

        return visitor.getCurrent();
    }

    private void enterColon() {
        checkStatus(OBJECT_NAME_SEPARATOR);
        setExpectedStatus(OBJECT_VALUE, ARRAY_VALUE, BEGIN_OBJECT, BEGIN_ARRAY);
    }

    private void enterComma() {
        if (hasExpectedStatus(END_ARRAY)) {
            setExpectedStatus(ARRAY_VALUE);
        } else if (hasExpectedStatus(END_OBJECT)) {
            setExpectedStatus(OBJECT_NAME);
        } else {
            throwUnexpectedException();
        }
    }

    private void exitArray() {
        checkStatus(END_ARRAY);
        JsonArray array = (JsonArray) valueDeque.pollLast();
        visitor.endArray(array);
        endCollection();
    }

    private void enterArray() {
        checkStatus(BEGIN_ARRAY);
        valueDeque.addLast(visitor.startArray());
        setExpectedStatus(END_ARRAY, ARRAY_VALUE);
    }

    private void enterObject() {
        checkStatus(BEGIN_OBJECT);
        valueDeque.addLast(visitor.startObject());
        setExpectedStatus(END_OBJECT, OBJECT_NAME);
    }

    public void exitObject() {
        checkStatus(END_OBJECT);
        JsonObject obj = valueDeque.pollLast().asObject();
        visitor.endObject(obj);
        endCollection();
    }

    private void endCollection() {
        if (valueDeque.size() > 0) {
            JsonValue peek = valueDeque.peek();
            if (peek.isObject()) {
                visitor.endObjectValue(peek.asObject(), nameDeque.pollLast());
            } else {
                visitor.endArrayElement(peek.asArray());
            }
        }
        setExpectedStatus(END_ARRAY, END_OBJECT, ELEMENT_SEPARATOR);
    }

    private void readTrue() throws IOException {
        if (hasExpectedStatus(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readTrueInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setExpectedStatus(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasExpectedStatus(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readTrueInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setExpectedStatus(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedException();
        }
    }

    private void readFalse() throws IOException {
        if (hasExpectedStatus(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readFalseInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setExpectedStatus(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasExpectedStatus(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readFalseInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setExpectedStatus(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedException();
        }
    }

    private void readNull() throws IOException {
        if (hasExpectedStatus(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readNullInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setExpectedStatus(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasExpectedStatus(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readNullInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setExpectedStatus(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedException();
        }
    }

    private void readNumber() throws IOException {
        if (hasExpectedStatus(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readNumberInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setExpectedStatus(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasExpectedStatus(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readNumberInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setExpectedStatus(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedException();
        }
    }

    private void readString() throws IOException {
        if (hasExpectedStatus(OBJECT_NAME)) {
            visitor.startObjectName((JsonObject) valueDeque.peekLast());
            String name = readName();
            visitor.endObjectName((JsonObject) valueDeque.peekLast(), name);
            nameDeque.addLast(name);
            setExpectedStatus(OBJECT_NAME_SEPARATOR);
        } else if (hasExpectedStatus(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readStringInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setExpectedStatus(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasExpectedStatus(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readStringInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setExpectedStatus(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedException();
        }
    }

    private String readName() throws IOException {
        readNext();
        openValueBuffer();
        while (current != JSON_QUOTATION_MARK && current != -1) {
            readNext();
        }

        return closeValueBuffer();
    }

    private void readStringInternal() throws IOException {
        readNext();

        openValueBuffer();
        visitor.startString();
        while (current != JSON_QUOTATION_MARK && current != -1) {
            readNext();
        }
        visitor.endString(closeValueBuffer());
    }

    private void readNumberInternal() throws IOException {
        openValueBuffer();
        visitor.startNumber();
        while (current >= '0' && current <= '9') {
            readNext();
        }
        visitor.endNumber(closeValueBuffer());
    }

    private void readNullInternal() throws IOException {
        visitor.startNull();
        readNext();
        isEqualsOrThrow('u', true);
        isEqualsOrThrow('l', true);
        isEqualsOrThrow('l', false);
        visitor.endNull();
    }

    private void readTrueInternal() throws IOException {
        visitor.startBool();
        readNext();
        isEqualsOrThrow('r', true);
        isEqualsOrThrow('u', true);
        isEqualsOrThrow('e', false);
        visitor.endBool(true);
    }

    private void readFalseInternal() throws IOException {
        visitor.startBool();
        readNext();
        isEqualsOrThrow('a', true);
        isEqualsOrThrow('l', true);
        isEqualsOrThrow('s', true);
        isEqualsOrThrow('e', false);
        visitor.endBool(false);
    }

    private void isEqualsOrThrow(char excepted, boolean next) throws IOException {
        if (current != excepted) {
            throwUnexpectedException(excepted);
        }

        if (next) {
            readNext();
        }
    }

    private void setExpectedStatus(int... excepted) {
        expectedStatus = 0;
        for (int e : excepted) {
            expectedStatus |= e;
        }
    }

    private boolean hasExpectedStatus(int actual) {
        return (expectedStatus & actual) != 0;
    }

    private void checkStatus(int... actual) {
        for (int a : actual) {
            if (hasExpectedStatus(a)) {
                return;
            }
        }

        throwUnexpectedException();
    }

    private void readNext() throws IOException {
        if (nextPos == fill) {
            if (valueStartPos != -1) {
                valueBuffer.append(readBuffer, valueStartPos, fill - valueStartPos);
                valueStartPos = 0;
            }

            //读取字符到缓存中
            fill = reader.read(readBuffer, 0, readBuffer.length);
            nextPos = 0;

            if (fill == -1) {
                current = -1;
                nextPos++;
                return;
            }
        }

        current = readBuffer[nextPos++];
        column++;

        if (current == NEW_LINE) {
            line++;
            column = 0;
        }
    }

    private boolean isWhitespace() {
        return current == WHITE_SPACE || current == NEW_LINE || current == LINE_FEED || current == TAB;
    }

    private void openValueBuffer() {
        if (valueBuffer == null) {
            valueBuffer = new StringBuilder();
        }

        valueStartPos = nextPos - 1;
    }

    private String closeValueBuffer() {
        if (valueBuffer.length() > 0) {
            valueBuffer.append(readBuffer, valueStartPos, nextPos - valueStartPos - 1);

            String value = valueBuffer.toString();
            valueBuffer.setLength(0);
            valueStartPos = -1;
            return value;
        }

        String val = new String(readBuffer, valueStartPos, nextPos - valueStartPos - 1);
        valueStartPos = -1;
        return val;
    }

    private void throwUnexpectedException(char excepted) {
        String msg = "Excepted \"" + excepted +
                "\" but got \"" + (char) current + "\"" +
                ". line: " + line + ", column: " + column;

        throw new JsonException(msg);
    }

    private void throwUnexpectedException() {
        StringBuilder s = new StringBuilder("Expected: ");
        int e = expectedStatus;
        while (e > 0) {
            int i = Integer.highestOneBit(e);
            s.append("\"").append(HITS.get(i)).append("\"").append(" , ");
            e -= i;
        }

        s.append("but got: ").append("\"").append((char) current).append("\". line: ")
                .append(line).append(", column: ").append(column);

        throw new JsonException(s.toString());
    }
}
