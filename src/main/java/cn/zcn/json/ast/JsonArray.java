package cn.zcn.json.ast;

import java.util.*;
import java.util.function.Consumer;

/**
 * 用于表示 JSON Array 的类
 *
 * @author zicung
 */
public class JsonArray extends JsonValue implements Collection<JsonValue> {

    private final List<JsonValue> values = new ArrayList<>();

    public JsonArray() {

    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public JsonArray asArray() {
        return this;
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code string}
     *
     * @param val value
     */
    public void addString(String val) {
        values.add(new JsonPrimitive(val));
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code string}
     *
     * @param val 待添加的值
     */
    public void addShort(short val) {
        values.add(new JsonPrimitive(val));
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code string}
     *
     * @param val 待添加的值
     */
    public void addInteger(int val) {
        values.add(new JsonPrimitive(val));
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code string}
     *
     * @param val 待添加的值
     */
    public void addDouble(double val) {
        values.add(new JsonPrimitive(val));
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code string}
     *
     * @param val 待添加的值
     */
    public void addFloat(float val) {
        values.add(new JsonPrimitive(val));
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code JsonObject}
     *
     * @param obj 待添加的  {@code JsonObject}
     */
    public void addObject(JsonObject obj) {
        values.add(obj);
    }

    /**
     * 在 JSON Array 的尾部追加一个 {@code JsonArray}
     *
     * @param arr 待添加的 {@code JsonArray}
     */
    public void addArray(JsonArray arr) {
        values.add(arr);
    }

    /**
     * 获取指定位置上的 JSON Value。
     *
     * @param index 索引
     * @return JsonValue
     */
    public JsonValue get(int index) {
        return values.get(index);
    }

    /**
     * 获取指定位置上的 JSON Value，并转化为 {@code JsonObject}
     *
     * @param index index
     * @return JsonObject
     */
    public JsonObject getAsObject(int index) {
        return (JsonObject) values.get(index);
    }

    /**
     * 获取指定位置上的 JSON Value，并转化为 {@code JsonArray}
     *
     * @param index index
     * @return JsonArray
     */
    public JsonArray getAsArray(int index) {
        return (JsonArray) values.get(index);
    }

    /**
     * 获取指定位置上的 JSON Value，并转化为 {@code JsonPrimitive}
     *
     * @param index index
     * @return JsonPrimitive
     */
    public JsonPrimitive getAsPrimitive(int index) {
        return (JsonPrimitive) values.get(index);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return values.contains(o);
    }

    @Override
    public Object[] toArray() {
        return values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return values.toArray(a);
    }

    @Override
    public boolean add(JsonValue val) {
        return values.add(val);
    }

    @Override
    public boolean remove(Object o) {
        return values.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return values.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends JsonValue> c) {
        return values.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return values.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return values.retainAll(c);
    }

    @Override
    public Iterator<JsonValue> iterator() {
        return values.iterator();
    }

    @Override
    public void forEach(Consumer<? super JsonValue> action) {
        values.forEach(action);
    }

    @Override
    public Spliterator<JsonValue> spliterator() {
        return values.spliterator();
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof JsonArray)) return false;
        return ((JsonArray) o).values.equals(values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}
