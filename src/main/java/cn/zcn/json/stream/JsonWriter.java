package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import static cn.zcn.json.ast.JsonCharacters.*;

/**
 * Json writer
 *
 * @author zicung
 */
public class JsonWriter {

    private final Writer out;

    /**
     * 当前缩进的深度
     */
    private int depth = 0;

    /**
     * 缩进符
     */
    private String indent = DEFAULT_INDENT;

    /**
     * 是否压缩 JSON
     */
    private boolean isCompressed = true;

    public JsonWriter(Writer out) {
        this.out = out;
    }

    public void setCompressed(boolean compressed) {
        isCompressed = compressed;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public void write(JsonValue json) {
        try {
            if (json instanceof JsonPrimitive) {
                writePrimitive(json.asPrimitive());
            } else if (json instanceof JsonObject) {
                writeObject(json.asObject());
            } else {
                writeArray(json.asArray());
            }
        } catch (IOException e) {
            throw new JsonException("Failed to write json.", e);
        }
    }

    private void writePrimitive(JsonPrimitive json) throws IOException {
        if (json.isNull()) {
            writeNull();
        } else if (json.isString()) {
            writeString(json.getAsString());
        } else if (json.isBool()) {
            writeBool(json.getAsBool());
        } else {
            writeNumber(json.getAsNumber());
        }
    }

    private void writeObject(JsonObject json) throws IOException {
        out.write(JSON_OBJECT_BEGIN);

        if (json.size() > 0) {
            increaseDepth();
            writeNewLine();
            writeIndent();

            Iterator<String> iter = json.keySet().iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                writeString(name);
                writePairSeparator();
                JsonValue value = json.get(name);
                write(value);

                if (iter.hasNext()) {
                    writeElementSeparator();
                }
            }

            decreaseDepth();
            writeNewLine();
            writeIndent();
        }

        out.write(JSON_OBJECT_END);
    }

    private void writeArray(JsonArray json) throws IOException {
        out.write(JSON_ARRAY_BEGIN);

        if (json.size() > 0) {
            increaseDepth();
            writeNewLine();
            writeIndent();

            Iterator<JsonValue> iter = json.iterator();
            while (iter.hasNext()) {
                write(iter.next());
                if (iter.hasNext()) {
                    writeElementSeparator();
                }
            }

            decreaseDepth();
            writeNewLine();
            writeIndent();
        }

        out.write(JSON_ARRAY_END);
    }

    private void writeNull() throws IOException {
        out.write(JSON_NULL);
    }

    private void writeString(String value) throws IOException {
        out.write(JSON_QUOTATION_MARK);
        out.write(value);
        out.write(JSON_QUOTATION_MARK);
    }

    private void writeBool(Boolean value) throws IOException {
        out.write(value.toString());
    }

    private void writeNumber(Number number) throws IOException {
        if (number instanceof Double) {
            if (((Double) number).isInfinite() || ((Double) number).isNaN()) {
                out.write(JSON_NULL);
            }
        } else if (number instanceof Float) {
            if (((Float) number).isInfinite() || ((Float) number).isNaN()) {
                out.write(JSON_NULL);
            }
        } else {
            out.write(number.toString());
        }
    }

    private void writePairSeparator() throws IOException {
        if (isCompressed) {
            out.write(JSON_NAME_SEPARATOR);
        } else {
            out.write(JSON_NAME_SEPARATOR);
            out.write(WHITE_SPACE);
        }
    }

    private void writeElementSeparator() throws IOException {
        out.write(JSON_VALUE_SEPARATOR);
        writeNewLine();
        writeIndent();
    }

    private void writeNewLine() throws IOException {
        if (!isCompressed) {
            out.write(LINE_FEED);
            out.write(NEW_LINE);
        }
    }

    private void writeIndent() throws IOException {
        if (!isCompressed) {
            for (int i = 0; i < depth; i++) {
                out.write(indent);
            }
        }
    }

    private void increaseDepth() {
        depth++;
    }

    private void decreaseDepth() {
        if (--depth < 0) {
            depth = 0;
        }
    }
}
