package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static cn.zcn.json.ast.JsonCharacters.*;

/**
 * 使用迭代的方式解析 JSON
 *
 * @author zicung
 */
public class JsonReader extends AbstractReader {

    /**
     * 使用二进制判断当前迭代的状态。
     * 使用 "|" 让二进制中的每一位的 "1" 表示其中一个状态。 e.g. 001 | 010 = 011
     * 利用 "&" 可以快速判断出当前状态与期望状态是否匹配。   e.g. 011 & 001 = 001 != 0
     */
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
     * 下一个期待的状态
     */
    private int nextState;

    public JsonReader(Reader reader, JsonListener listener) {
        super(reader, listener);
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

            skipWhiteSpace();

            switch (current) {
                case -1:
                    checkState(END_DOCUMENT);
                    return listener.getRoot();
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
        listener.endArray();
        endCollection();
    }

    private void enterArray() {
        checkState(BEGIN_ARRAY);
        listener.startArray();
        setNextState(END_ARRAY, ARRAY_VALUE, BEGIN_OBJECT);
    }

    private void enterObject() {
        checkState(BEGIN_OBJECT);
        listener.startObject();
        setNextState(END_OBJECT, OBJECT_NAME);
    }

    public void exitObject() {
        checkState(END_OBJECT);
        listener.endObject();
        endCollection();
    }

    private void endCollection() {
        if (listener.size() > 0) {
            if (listener.peekLast().isObject()) {
                listener.endObjectValue();
                setNextState(END_OBJECT, ELEMENT_SEPARATOR);
            } else {
                listener.endArrayElement();
                setNextState(END_ARRAY, ELEMENT_SEPARATOR);
            }
        } else {
            setNextState(END_DOCUMENT);
        }
    }

    private void readTrue() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            listener.startObjectValue();
            readTrueInternal();
            listener.endObjectValue();
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            listener.startArrayElement();
            readTrueInternal();
            listener.endArrayElement();
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readFalse() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            listener.startObjectValue();
            readFalseInternal();
            listener.endObjectValue();
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            listener.startArrayElement();
            readFalseInternal();
            listener.endArrayElement();
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readNull() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            listener.startObjectValue();
            readNullInternal();
            listener.endObjectValue();
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            listener.startArrayElement();
            readNullInternal();
            listener.endArrayElement();
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readNumber() throws IOException {
        if (hasState(OBJECT_VALUE)) {
            listener.startObjectValue();
            readNumberInternal();
            listener.endObjectValue();
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            listener.startArrayElement();
            readNumberInternal();
            listener.endArrayElement();
            setNextState(END_ARRAY, ELEMENT_SEPARATOR);
        } else {
            throwUnexpectedStateException();
        }
    }

    private void readString() throws IOException {
        if (hasState(OBJECT_NAME)) {
            listener.startObjectName();
            String name = readName();
            listener.endObjectName(name);
            setNextState(OBJECT_NAME_SEPARATOR);
        } else if (hasState(OBJECT_VALUE)) {
            listener.startObjectValue();
            readStringInternal();
            listener.endObjectValue();
            setNextState(END_OBJECT, ELEMENT_SEPARATOR);
        } else if (hasState(ARRAY_VALUE)) {
            listener.startArrayElement();
            readStringInternal();
            listener.endArrayElement();
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
