package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.io.IOException;
import java.io.Reader;

import static cn.zcn.json.ast.JsonCharacters.*;

/**
 * Json Reader
 *
 * @author zicung
 */
public class JsonReader {

    /**
     * Json content stream
     */
    private final Reader reader;

    /**
     * 词法遍历器
     */
    private final JsonVisitor visitor;

    /**
     * 字符缓存，用于缓存从 {@code Reader} 读取的 chars
     */
    private char[] readBuffer;

    /**
     * 下一个将要读取的 char 的索引位置
     */
    private int nextPos;

    /**
     * {@code readBuffer} 缓存的字符数量
     */
    private int fillSize;

    /**
     * 当前读取的 char
     */
    private int current;

    /**
     * Json Value buffer。当在读取 Json Value 时读取到了 {@code readBuffer} 的尾部时，当前 Json Value 尚未读取完成时，将内容缓存到 {@code valueBuffer}
     */
    private StringBuilder valueBuffer;

    /**
     * 标识当前读取的 Json Value 的开始索引
     */
    private int valueStartPos;

    public JsonReader(Reader reader, JsonVisitor visitor) {
        this.reader = reader;
        this.visitor = visitor;
    }

    public JsonValue read() {
        try {
            nextPos = 0;
            readBuffer = new char[512];
            current = 0;
            valueStartPos = -1;

            readNext(true);

            if (fillSize == -1) {
                throw new JsonException("Empty json string.");
            } else if (current == JSON_OBJECT_BEGIN) {
                readJsonObject();
            } else if (current == JSON_ARRAY_BEGIN) {
                readJsonArray();
            } else {
                throw new JsonException("Json is a object or array.");
            }

            skipWhiteSpace();
            if (fillSize != -1) {
                throw new JsonException("Invalid json ending.");
            }

            return visitor.getCurrent();
        } catch (IOException e) {
            throw new JsonException("Failed to read json.", e);
        }
    }

    private void readValue() throws IOException {
        switch (current) {
            case JSON_OBJECT_BEGIN:
                readJsonObject();
                break;
            case JSON_ARRAY_BEGIN:
                readJsonArray();
                break;
            case JSON_QUOTATION_MARK:
                readString();
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
                break;
            default:
                throw new JsonException("Unsupported json value: " + (char) current);
        }
    }

    private void readJsonObject() throws IOException {
        JsonObject jsonObject = visitor.startObject();

        readNext(true);
        if (current == JSON_OBJECT_END) {
            visitor.endObject(jsonObject);
            readNext(true);
            return;
        }

        do {
            skipWhiteSpace();
            readNext(true);

            visitor.startObjectName(jsonObject);
            String name = readName();
            visitor.endObjectName(jsonObject, name);

            isEqualsOrThrow(JSON_NAME_SEPARATOR);
            skipWhiteSpace();

            visitor.startObjectValue(jsonObject, name);
            readValue();
            visitor.endObjectValue(jsonObject, name);
        } while (isEquals(JSON_VALUE_SEPARATOR));

        isEqualsOrThrow(JSON_OBJECT_END);
        visitor.endObject(jsonObject);
    }

    private void readJsonArray() throws IOException {
        JsonArray array = visitor.startArray();

        readNext(true);
        do {
            skipWhiteSpace();
            visitor.startArrayElement(array);
            readValue();
            visitor.endArrayElement(array);
        } while (isEquals(JSON_VALUE_SEPARATOR));

        isEqualsOrThrow(JSON_ARRAY_END);
        visitor.endArray(array);
    }

    /**
     * 读取 json string value。读取完后会读下一个字符（跳过空白字符）。
     */
    private void readString() throws IOException {
        readNext(false);

        startValueBuffer();
        visitor.startString();

        //TODO 处理 "\"、"unicode"
        while (current != JSON_QUOTATION_MARK && current != -1) {
            readNext(false);
        }
        String value = endValueBuffer();
        visitor.endString(value);

        readNext(true);
    }

    /**
     * 读取 json null value。读取完后会读取 {@code null} 的下一个字符（跳过空白字符）。
     */
    private void readNull() throws IOException {
        visitor.startNull();
        readNext(false);
        isEqualsOrThrow('u');
        isEqualsOrThrow('l');
        isEqualsOrThrow('l');
        visitor.endNull();

        skipWhiteSpace();
    }

    /**
     * 读取 json false value。读取完后会读取 {@code false} 的下一个字符（跳过空白字符）。
     */
    private void readFalse() throws IOException {
        visitor.startBool();
        readNext(false);
        isEqualsOrThrow('a');
        isEqualsOrThrow('l');
        isEqualsOrThrow('s');
        isEqualsOrThrow('e');
        visitor.endBool(false);

        skipWhiteSpace();
    }

    /**
     * 读取 json true value。读取完后会读取 {@code true} 的下一个字符（跳过空白字符）。
     */
    private void readTrue() throws IOException {
        visitor.startBool();
        readNext(false);
        isEqualsOrThrow('r');
        isEqualsOrThrow('u');
        isEqualsOrThrow('e');
        visitor.endBool(true);

        skipWhiteSpace();
    }

    /**
     * 读取 json number value。读取完后会读取下一个字符（跳过空白字符）。
     */
    private void readNumber() throws IOException {
        visitor.startNumber();
        startValueBuffer();
        while (current >= '0' && current <= '9') {
            readNext(false);
        }
        String num = endValueBuffer();
        visitor.endNumber(num);

        skipWhiteSpace();
    }

    /**
     * 读取 json member name，读取完后会读取 name 的下一个字符（跳过空白字符）。
     */
    private String readName() throws IOException {
        startValueBuffer();
        while (current != JSON_QUOTATION_MARK && current != -1) {
            readNext(false);
        }
        String name = endValueBuffer();
        readNext(true);
        return name;
    }

    private void skipWhiteSpace() throws IOException {
        while (current == WHITE_SPACE || current == NEW_LINE || current == LINE_FEED || current == TAB) {
            readNext(false);
        }
    }

    private void startValueBuffer() {
        if (valueBuffer == null) {
            valueBuffer = new StringBuilder();
        }

        valueStartPos = nextPos - 1;
    }

    private String endValueBuffer() {
        if (valueBuffer.length() > 0) {
            valueBuffer.append(readBuffer, valueStartPos, nextPos - valueStartPos - 1);

            String value = valueBuffer.toString();
            valueBuffer.setLength(0);
            return value;
        }

        return new String(readBuffer, valueStartPos, nextPos - valueStartPos - 1);
    }

    /**
     * 判断 {@code current} 与 {@code excepted} 是否相等，如果相等会读取下一个字符。
     *
     * @return 返回 {@code false},如果 {@code current == excepted}；返回 {@code true},{@code current != excepted}；
     */
    private boolean isEquals(char excepted) throws IOException {
        if (current != excepted) {
            return false;
        }

        readNext(false);
        return true;
    }

    /**
     * 判断 {@code current} 与 {@code required} 是否相等，如果不相等抛出异常。
     * 如果相等会读取下一个字符。
     */
    private void isEqualsOrThrow(char excepted) throws IOException {
        if (current != excepted) {
            throwUnexpectedCharException(excepted);
        }

        readNext(false);
    }

    /**
     * 读取下一个字符
     */
    private void readNext(boolean skipWhitespace) throws IOException {
        if (nextPos == fillSize) {
            if (valueStartPos != -1) {
                valueBuffer.append(readBuffer, valueStartPos, fillSize - valueStartPos);
                valueStartPos = 0;
            }

            //读取字符到缓存中
            fillSize = reader.read(readBuffer, 0, readBuffer.length);
            nextPos = 0;

            if (fillSize == -1) {
                current = -1;
                nextPos++;
                return;
            }
        }

        current = readBuffer[nextPos++];

        if (skipWhitespace) {
            skipWhiteSpace();
        }
    }

    private void throwUnexpectedCharException(char expected) {
        throw new JsonException("Expected " + expected + " but got " + (char) current);
    }
}
