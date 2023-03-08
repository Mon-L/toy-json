package cn.zcn.json.ast;

/**
 * 用于表示 JSON Literal 的类，可以是一个 {@code null}、{@link Boolean}、{@link String}、{@link Number}
 *
 * @author zicung
 */
public class JsonPrimitive extends JsonValue {

    /**
     * 用于表示 JSON Literal {@code null}
     */
    public static final JsonPrimitive NULL = new JsonPrimitive();

    /**
     * 用于表示 JSON Literal {@code true}
     */
    public static final JsonPrimitive TRUE = new JsonPrimitive(true);

    /**
     * 用于表示 JSON Literal {@code false}
     */
    public static final JsonPrimitive FALSE = new JsonPrimitive(false);

    /**
     * 标识 JSON Primitive 是否是一个 {@code Boolean}
     */
    private final boolean isBool;

    /**
     * 标识 JSON Primitive 是否是一个 {@code String}
     */
    private final boolean isString;

    /**
     * 标识 JSON Primitive 是否是一个 {@code Number}
     */
    private final boolean isNumber;

    private Object value;

    private JsonPrimitive() {
        this.isBool = false;
        this.isString = false;
        this.isNumber = false;
    }

    private JsonPrimitive(Boolean bool) {
        this.isBool = true;
        this.isString = false;
        this.isNumber = false;
        this.value = bool;
    }

    public JsonPrimitive(String string) {
        this.isBool = false;
        this.isString = true;
        this.isNumber = false;
        this.value = string;
    }

    public JsonPrimitive(Number number) {
        this.isBool = false;
        this.isString = false;
        this.isNumber = true;
        this.value = number;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public boolean isBool() {
        return isBool;
    }

    @Override
    public boolean isTrue() {
        return this == TRUE;
    }

    @Override
    public boolean isFalse() {
        return this == FALSE;
    }

    @Override
    public boolean isNumber() {
        return isNumber;
    }

    @Override
    public boolean isString() {
        return isString;
    }

    @Override
    public Boolean getAsBool() {
        if (isBool()) {
            return (Boolean) value;
        }

        throw new UnsupportedOperationException("Json primitive is not a bool.");
    }

    @Override
    public String getAsString() {
        if (isString()) {
            return (String) value;
        }

        throw new UnsupportedOperationException("Json primitive is not a string.");
    }

    @Override
    public Number getAsNumber() {
        if (isNumber()) {
            return (Number) value;
        }

        throw new UnsupportedOperationException("Json primitive is not a number.");
    }

    @Override
    public Integer getAsInteger() {
        if (isNumber()) {
            return getAsNumber().intValue();
        }

        throw new UnsupportedOperationException("Json primitive is not a integer.");
    }

    @Override
    public Float getAsFloat() {
        if (isNumber()) {
            return getAsNumber().floatValue();
        }

        throw new UnsupportedOperationException("Json primitive is not a float.");
    }

    @Override
    public Double getAsDouble() {
        if (isNumber()) {
            return getAsNumber().doubleValue();
        }

        throw new UnsupportedOperationException("Json primitive is not a double.");
    }

    @Override
    public Short getAsShort() {
        if (isNumber()) {
            return getAsNumber().shortValue();
        }

        throw new UnsupportedOperationException("Json primitive is not a short.");
    }

    @Override
    public JsonPrimitive asPrimitive() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof JsonPrimitive)) return false;
        if (((JsonPrimitive) o).value == null || value == null)
            return ((JsonPrimitive) o).value == null && value == null;
        return value.equals(((JsonPrimitive) o).value);
    }

    @Override
    public int hashCode() {
        if (value == null) return 31;
        return value.hashCode();
    }
}
