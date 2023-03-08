package cn.zcn.json.ast;

import cn.zcn.json.Json;

import java.util.*;

/**
 * 用于表示 JSON Object 的类
 *
 * @author zicung
 */
public class JsonObject extends JsonValue {

    private final Map<String, JsonValue> values = new LinkedHashMap<>();

    public JsonObject() {
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public JsonObject asObject() {
        return this;
    }

    /**
     * 设置一个 JSON member。如果存在相同的 {@code name} 则覆盖旧的 {@code value}。
     *
     * @param name  name
     * @param value value
     */
    public void set(String name, JsonValue value) {
        values.put(name, value);
    }

    public void set(String name, String value) {
        values.put(name, new JsonPrimitive(value));
    }

    public void set(String name, boolean value) {
        values.put(name, value ? JsonPrimitive.TRUE : JsonPrimitive.FALSE);
    }

    public void set(String name, int value) {
        values.put(name, new JsonPrimitive(value));
    }

    public void set(String name, float value) {
        values.put(name, new JsonPrimitive(value));
    }

    public void set(String name, double value) {
        values.put(name, new JsonPrimitive(value));
    }

    /**
     * 获取一个 {@code JsonValue}。
     *
     * @param name name
     */
    public JsonValue get(String name) {
        return values.get(name);
    }

    /**
     * 获取一个 {@code JsonObject} 表示的 JSON Value。
     *
     * @param name name
     */
    public JsonObject getAsObject(String name) {
        return (JsonObject) values.get(name);
    }

    /**
     * 获取一个 {@code JsonArray} 表示的 JSON Value。
     *
     * @param name name
     */
    public JsonArray getAsArray(String name) {
        return (JsonArray) values.get(name);
    }

    /**
     * 获取一个 {@code JsonPrimitive} 表示的 JSON Value。
     *
     * @param name name
     */
    public JsonPrimitive getAsPrimitive(String name) {
        return (JsonPrimitive) values.get(name);
    }

    /**
     * 移除一个 JSON Value.
     *
     * @param name name
     * @return 被移除的 JSON member
     */
    public JsonValue remove(String name) {
        return values.remove(name);
    }

    /**
     * 是否存在指定 {@code name} 的 JSON Value.
     *
     * @param name name
     * @return 存在指定 {@code name} 的 JSON member，返回 {@code true}
     */
    public boolean has(String name) {
        return values.containsKey(name);
    }

    /**
     * JSON Object 包含的 JSON member 的数量。
     *
     * @return JSON member size
     */
    public int size() {
        return values.size();
    }

    /**
     * JSON Object 中 JSON member 的数量是否为零。
     *
     * @return JSON member 的数量是否为零返回 {@code true}
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Set<String> keySet() {
        return values.keySet();
    }

    public Set<Map.Entry<String, JsonValue>> entrySet() {
        return values.entrySet();
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof JsonObject)) {
            return false;
        }

        return ((JsonObject) o).values.equals(values);
    }
}
