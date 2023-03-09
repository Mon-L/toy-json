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
public class JsonReader extends AbstractReader {

    /**
     * 词法遍历器
     */
    private final JsonVisitor visitor;

    public JsonReader(Reader reader, JsonVisitor visitor) {
        super(reader);
        this.visitor = visitor;
    }

    public JsonValue read() {
        try {
            nextPos = 0;
            current = 0;
            valueStartPos = -1;

            readNext();
            skipWhiteSpace();

            if (fill == -1) {
                throw new JsonException("Empty json string.");
            } else if (current == JSON_OBJECT_BEGIN) {
                readJsonObject();
            } else if (current == JSON_ARRAY_BEGIN) {
                readJsonArray();
            } else {
                throw new JsonException("Json is a object or array.");
            }

            skipWhiteSpace();
            if (fill != -1) {
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

        readNext();
        skipWhiteSpace();

        if (current == JSON_OBJECT_END) {
            visitor.endObject(jsonObject);
            readNext();
            skipWhiteSpace();
            return;
        }

        do {
            skipWhiteSpace();
            readNext();
            skipWhiteSpace();

            visitor.startObjectName(jsonObject);
            String name = readName();
            visitor.endObjectName(jsonObject, name);

            isEqualsOrThrow(JSON_NAME_SEPARATOR);
            skipWhiteSpace();

            visitor.startObjectValue(jsonObject, name);
            readValue();
            visitor.endObjectValue(jsonObject, name);
            skipWhiteSpace();
        } while (isEquals(JSON_VALUE_SEPARATOR));

        isEqualsOrThrow(JSON_OBJECT_END);
        visitor.endObject(jsonObject);
    }

    private void readJsonArray() throws IOException {
        JsonArray array = visitor.startArray();

        readNext();
        skipWhiteSpace();
        do {
            skipWhiteSpace();
            visitor.startArrayElement(array);
            readValue();
            visitor.endArrayElement(array);
            skipWhiteSpace();
        } while (isEquals(JSON_VALUE_SEPARATOR));

        isEqualsOrThrow(JSON_ARRAY_END);
        visitor.endArray(array);
    }

    /**
     * 读取 json string value。读取完后会读下一个字符（跳过空白字符）。
     */
    private void readString() throws IOException {
        readNext();

        openValueBuffer();
        visitor.startString();

        //TODO 处理 "\"、"unicode"
        while (current != JSON_QUOTATION_MARK && current != -1) {
            readNext();
        }
        String value = closeValueBuffer();
        visitor.endString(value);

        readNext();
        skipWhiteSpace();
    }

    /**
     * 读取 json null value。读取完后会读取 {@code null} 的下一个字符（跳过空白字符）。
     */
    private void readNull() throws IOException {
        visitor.startNull();
        readNext();
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
        readNext();
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
        readNext();
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
        openValueBuffer();
        while (current >= '0' && current <= '9') {
            readNext();
        }
        String num = closeValueBuffer();
        visitor.endNumber(num);

        skipWhiteSpace();
    }

    /**
     * 读取 json member name，读取完后会读取 name 的下一个字符（跳过空白字符）。
     */
    private String readName() throws IOException {
        openValueBuffer();
        while (current != JSON_QUOTATION_MARK && current != -1) {
            readNext();
        }
        String name = closeValueBuffer();
        readNext();
        skipWhiteSpace();
        return name;
    }

    private void skipWhiteSpace() throws IOException {
        while (isWhitespace()) {
            readNext();
        }
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

        readNext();
        return true;
    }

    /**
     * 判断 {@code current} 与 {@code expected} 是否相等，如果不相等抛出异常。
     * 如果相等会读取下一个字符。
     */
    private void isEqualsOrThrow(char expected) throws IOException {
        if (current != expected) {
            throw new UnexpectedException(expected, current, line, column);
        }

        readNext();
    }
}
