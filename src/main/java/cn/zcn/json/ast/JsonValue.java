package cn.zcn.json.ast;

import cn.zcn.json.stream.JsonWriter;

import java.io.StringWriter;

/**
 * 用于表示 JSON Value 的类，可以是一个 {@link JsonPrimitive}、{@link JsonObject}、{@link JsonArray}
 *
 * @author zicung
 */
public class JsonValue {

    protected JsonValue() {
    }

    /**
     * 检查 JSON Value 是不是一个 {@code Boolean}。
     *
     * @return 如果是布尔值返回 {@code ture}。
     */
    public boolean isBool() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@code true}。
     *
     * @return 如果是 {@code true} 返回 {@code ture}。
     */
    public boolean isTrue() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@code false}。
     *
     * @return 如果是 {@code false} 返回 {@code ture}。
     */
    public boolean isFalse() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@code Number}。
     *
     * @return 如果是 {@code Number} 返回 {@code ture}。
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@code String}。
     *
     * @return 如果是 {@code String} 返回 {@code ture}。
     */
    public boolean isString() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@code null}。
     *
     * @return 如果是 {@code null} 返回 {@code ture}。
     */
    public boolean isNull() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@link JsonPrimitive}。
     *
     * @return 如果是 {@code JsonPrimitive} 返回 {@code ture}。
     */
    public boolean isPrimitive() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@link JsonObject}。
     *
     * @return 如果是 {@code JsonObject} 返回 {@code ture}。
     */
    public boolean isObject() {
        return false;
    }

    /**
     * 检查 JSON Value 是不是一个 {@link JsonArray}。
     *
     * @return 如果是 {@code JsonArray} 返回 {@code ture}。
     */
    public boolean isArray() {
        return false;
    }

    /**
     * 返回 {@code Boolean} 表示的 JSON Value。如果不是一个 {@code Boolean} 则抛出异常。
     *
     * @return {@code Boolean}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code Boolean}
     */
    public Boolean getAsBool() {
        throw new UnsupportedOperationException("Json value is not a bool.");
    }

    /**
     * 返回 {@code String} 表示的 JSON Value。如果不是一个 {@code String} 则抛出异常。
     *
     * @return {@code String}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code String}
     */
    public String getAsString() {
        throw new UnsupportedOperationException("Json value is not a string.");
    }

    /**
     * 返回 {@code Number} 表示的 JSON Value。如果不是一个 {@code Number} 则抛出异常。
     *
     * @return {@code Number}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code Number}
     */
    public Number getAsNumber() {
        throw new UnsupportedOperationException("Json value is not a number.");
    }

    /**
     * 返回 {@code Integer} 表示的 JSON Value。如果不是一个 {@code Integer} 则抛出异常。
     *
     * @return {@code Integer}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code Integer}
     */
    public Integer getAsInteger() {
        throw new UnsupportedOperationException("Json value is not a integer.");
    }

    /**
     * 返回 {@code Float} 表示的 JSON Value。如果不是一个 {@code Float} 则抛出异常。
     *
     * @return {@code Float}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code Float}
     */
    public Float getAsFloat() {
        throw new UnsupportedOperationException("Json value is not a float.");
    }

    /**
     * 返回 {@code Double} 表示的 JSON Value。如果不是一个 {@code Double} 则抛出异常。
     *
     * @return {@code Double}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code Double}
     */
    public Double getAsDouble() {
        throw new UnsupportedOperationException("Json value is not a double.");
    }

    /**
     * 返回 {@code Short} 表示的 JSON Value。如果不是一个 {@code Short} 则抛出异常。
     *
     * @return {@code Short}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code Short}
     */
    public Short getAsShort() {
        throw new UnsupportedOperationException("Json value is not a short.");
    }

    /**
     * 返回 {@code JsonObject} 表示的 JSON Value。如果不是一个 {@code JsonObject} 则抛出异常。
     *
     * @return {@code JsonObject}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code JsonObject}
     */
    public JsonObject asObject() {
        throw new UnsupportedOperationException("Json value is not a JsonObject.");
    }

    /**
     * 返回 {@code JsonArray} 表示的 JSON Value。如果不是一个 {@code JsonArray} 则抛出异常。
     *
     * @return {@code JsonArray}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code JsonArray}
     */
    public JsonArray asArray() {
        throw new UnsupportedOperationException("Json value is not a JsonArray.");
    }

    /**
     * 返回 {@code JsonPrimitive} 表示的 JSON Value。如果不是一个 {@code JsonPrimitive} 则抛出异常。
     *
     * @return {@code JsonPrimitive}
     * @throws UnsupportedOperationException JSON Value 不是一个 {@code JsonPrimitive}
     */
    public JsonPrimitive asPrimitive() {
        throw new UnsupportedOperationException("Json value is not a JsonPrimitive.");
    }

    @Override
    public String toString() {
        StringWriter out = new StringWriter();
        JsonWriter writer = new JsonWriter(out);
        writer.write(this);
        return out.toString();
    }
}
