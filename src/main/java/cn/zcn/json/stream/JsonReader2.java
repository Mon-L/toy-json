package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.io.IOException;
import java.io.Reader;

import static cn.zcn.json.ast.JsonCharacters.*;

/**
 * 使用递归的方式解析 JSON
 *
 * @author zicung
 */
public class JsonReader2 extends AbstractReader {

    public JsonReader2(Reader reader, JsonListener listener) {
        super(reader, listener);
    }

    public JsonValue read() {
        try {
            readNextAndSkip();

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

            return listener.getRoot();
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
                readStringInternal();
                readNextAndSkip();
                break;
            case 't':
                readTrueInternal();
                break;
            case 'f':
                readFalseInternal();
                break;
            case 'n':
                readNullInternal();
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
                readNumberInternal();
                break;
            default:
                throw new JsonException("Unsupported json value: " + (char) current);
        }
    }

    private void readJsonObject() throws IOException {
        listener.startObject();

        readNextAndSkip();

        if (current == JSON_OBJECT_END) {
            listener.endObject();
            readNextAndSkip();
            return;
        }

        do {
            skipWhiteSpace();
            readNextAndSkip();

            listener.startObjectName();
            String name = readName();
            listener.endObjectName(name);

            isEqualsOrThrow(JSON_NAME_SEPARATOR);
            skipWhiteSpace();

            listener.startObjectValue();
            readValue();
            listener.endObjectValue();
            skipWhiteSpace();
        } while (isElementSeparator());

        isEqualsOrThrow(JSON_OBJECT_END);
        listener.endObject();
    }

    private void readJsonArray() throws IOException {
        listener.startArray();

        readNext();
        skipWhiteSpace();
        do {
            skipWhiteSpace();
            listener.startArrayElement();
            readValue();
            listener.endArrayElement();
            skipWhiteSpace();
        } while (isElementSeparator());

        isEqualsOrThrow(JSON_ARRAY_END);
        listener.endArray();
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
        readNextAndSkip();
        return name;
    }

    /**
     * 判断当前字符是否是 ","
     */
    private boolean isElementSeparator() throws IOException {
        if (current != JsonCharacters.JSON_VALUE_SEPARATOR) {
            return false;
        }

        readNext();
        return true;
    }

    private void readNextAndSkip() throws IOException {
        readNext();
        skipWhiteSpace();
    }
}
