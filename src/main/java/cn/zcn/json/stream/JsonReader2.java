package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static cn.zcn.json.ast.JsonCharacters.*;

/**
 * Json Reader 2
 *
 * @author zicung
 */
public class JsonReader2 extends AbstractReader {

    private static final int BEGIN_OBJECT = 0x001; // {
    private static final int END_OBJECT = 0x002; // }
    private static final int BEGIN_ARRAY = 0x004;  // [
    private static final int END_ARRAY = 0x008; // ]
    private static final int OBJECT_NAME_SEPARATOR = 0x010; // :
    private static final int ELEMENT_SEPARATOR = 0x020; // ,
    private static final int OBJECT_NAME = 0x040;
    private static final int OBJECT_VALUE = 0x080;
    private static final int ARRAY_VALUE = 0x100;
    private static final int END_DOCUMENT = 0x200;
    private static final Map<Integer, String> STATE_HITS = new HashMap<>();

    static {
        STATE_HITS.put(BEGIN_OBJECT, "{");
        STATE_HITS.put(END_OBJECT, "}");
        STATE_HITS.put(BEGIN_ARRAY, "[");
        STATE_HITS.put(END_ARRAY, "]");
        STATE_HITS.put(OBJECT_NAME_SEPARATOR, ":");
        STATE_HITS.put(ELEMENT_SEPARATOR, ",");
        STATE_HITS.put(OBJECT_NAME, "Pair Name");
        STATE_HITS.put(OBJECT_VALUE, "Pair Value");
        STATE_HITS.put(ARRAY_VALUE, "Array Element");
        STATE_HITS.put(END_DOCUMENT, "EOF");
    }

    /**
     * 词法遍历器
     */
    private final JsonVisitor visitor;

    /**
     * 下一个期待的状态
     */
    private int nextState;

    /**
     * 存储 JsonObject 和 JsonArray。
     * 当读取到 '['、'{' 时，入栈JsonObject、JsonArray。
     * 当读取到 '}'、']' 时，出栈。
     */
    private final Deque<JsonValue> valueDeque = new LinkedList<>();

    /**
     * 存储 Json Object 的键名。
     * 当读取到 pair name 时，入栈。
     * 当读取完 pair value，出栈。
     */
    private final Deque<String> nameDeque = new LinkedList<>();

    public JsonReader2(Reader reader, JsonVisitor visitor) {
        super(reader);
        this.visitor = visitor;
    }

    public JsonValue read() {
        try {
            return doRead();
        } catch (IOException e) {
            throw new JsonException("Failed to read json.", e);
        }
    }

    private JsonValue doRead() throws IOException {
        boolean readNext = true;
        setNextState(BEGIN_OBJECT, BEGIN_ARRAY);
        while (true) {
            if (readNext) {
                readNext();
            } else {
                readNext = true;
            }

            while (isWhitespace()) {
                readNext();
            }

            switch (current) {
                case -1:
                    checkState(END_DOCUMENT);
                    return visitor.getCurrent();
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
                    readNext = false;
                    break;
                case 'f':
                    readFalse();
                    readNext = false;
                    break;
                case 'n':
                    readNull();
                    readNext = false;
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
                    readNext = false;
                    break;
                default:
                    throw new JsonException("Unsupported json value prefix: " + (char) current);
            }
        }
    }

    private void enterColon() {
        checkState(OBJECT_NAME_SEPARATOR);
        setNextState(OBJECT_VALUE, BEGIN_OBJECT, BEGIN_ARRAY);
    }

    private void enterComma() {
        if (hasState(END_ARRAY)) {
            setNextState(ARRAY_VALUE, BEGIN_OBJECT, BEGIN_ARRAY);
        } else if (hasState(END_OBJECT)) {
            setNextState(OBJECT_NAME);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void exitArray() {
        checkState(END_ARRAY);
        JsonArray array = (JsonArray) valueDeque.pollLast();
        visitor.endArray(array);
        endCollection();
    }

    private void enterArray() {
        checkState(BEGIN_ARRAY);
        valueDeque.addLast(visitor.startArray());
        setNextState(END_ARRAY, ARRAY_VALUE, BEGIN_OBJECT);
    }

    private void enterObject() {
        checkState(BEGIN_OBJECT);
        valueDeque.addLast(visitor.startObject());
        setNextState(END_OBJECT, OBJECT_NAME);
    }

    public void exitObject() {
        checkState(END_OBJECT);
        JsonObject obj = (JsonObject) valueDeque.pollLast();
        visitor.endObject(obj);
        endCollection();
    }

    private void endCollection() {
        if (valueDeque.size() > 0) {
            JsonValue peek = valueDeque.peekLast();
            if (peek.isObject()) {
                visitor.endObjectValue(peek.asObject(), nameDeque.pollLast());
                setNextState(END_OBJECT, ELEMENT_SEPARATOR);
            } else {
                visitor.endArrayElement(peek.asArray());
                setNextState(END_ARRAY, ELEMENT_SEPARATOR);
            }
        } else {
            setNextState(END_DOCUMENT);
        }
    }

    private void readTrue() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readTrueInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readTrueInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readFalse() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readFalseInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readFalseInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readNull() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readNullInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readNullInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readNumber() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readNumberInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readNumberInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readString() throws IOException {
        if (hasState(OBJECT_NAME)) {
            visitor.startObjectName((JsonObject) valueDeque.peekLast());
            String name = readName();
            visitor.endObjectName((JsonObject) valueDeque.peekLast(), name);
            nameDeque.addLast(name);
            setNextState(OBJECT_NAME_SEPARATOR);
        } else if (hasState(OBJECT_VALUE)) {
            visitor.startObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.peekLast());
            readStringInternal();
            visitor.endObjectValue((JsonObject) valueDeque.peekLast(), nameDeque.pollLast());
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            visitor.startArrayElement((JsonArray) valueDeque.peekLast());
            readStringInternal();
            visitor.endArrayElement((JsonArray) valueDeque.peekLast());
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
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
        isEqualsOrThrow('u');
        isEqualsOrThrow('l');
        isEqualsOrThrow('l');
        visitor.endNull();
    }

    private void readTrueInternal() throws IOException {
        visitor.startBool();
        readNext();
        isEqualsOrThrow('r');
        isEqualsOrThrow('u');
        isEqualsOrThrow('e');
        visitor.endBool(true);
    }

    private void readFalseInternal() throws IOException {
        visitor.startBool();
        readNext();
        isEqualsOrThrow('a');
        isEqualsOrThrow('l');
        isEqualsOrThrow('s');
        isEqualsOrThrow('e');
        visitor.endBool(false);
    }

    private void isEqualsOrThrow(char expected) throws IOException {
        if (current != expected) {
            throw new UnexpectedException(expected, current, line, column);
        }

        readNext();
    }

    private void setNextState(int... newState) {
        this.nextState = 0;
        for (int s : newState) {
            this.nextState |= s;
        }
    }

    private boolean hasState(int actual) {
        return (nextState & actual) != 0;
    }

    private void checkState(int... actual) {
        for (int a : actual) {
            if (hasState(a)) {
                return;
            }
        }

        throwUnexpectedStateException();
    }

    private void throwUnexpectedStateException() {
        StringBuilder s = new StringBuilder("Expected: ");
        int e = nextState;
        while (e > 0) {
            int highestBit = Integer.highestOneBit(e);
            s.append("\"").append(STATE_HITS.get(highestBit)).append("\"").append(" , ");
            e -= highestBit;
        }

        s.append("but got: ").append("\"").append((char) current).append("\".Line: ")
                .append(line).append(", Column: ").append(column);

        throw new JsonException(s.toString());
    }
}
