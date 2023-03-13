package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

import java.util.Deque;
import java.util.LinkedList;

public class DefaultJsonListener extends JsonListener {

    /**
     * 存储 JsonObject 和 JsonArray。
     * 当读取到 '['、'{' 时，入栈JsonObject、JsonArray。
     * 当读取到 '}'、']' 时，出栈。
     */
    private final Deque<JsonValue> values = new LinkedList<>();

    /**
     * 存储 Json Object 的键名。
     * 当读取到 pair name 时，入栈。
     * 当读取完 pair value，出栈。
     */
    private final Deque<String> names = new LinkedList<>();

    private JsonValue pre;

    @Override
    public void endNull() {
        values.offerLast(JsonPrimitive.NULL);
    }

    @Override
    public void endBool(boolean bool) {
        values.offerLast(bool ? JsonPrimitive.TRUE : JsonPrimitive.FALSE);
    }

    @Override
    public void endString(String string) {
        values.offerLast(new JsonPrimitive(string));
    }

    @Override
    public void endNumber(String number) {
        values.offerLast(new JsonPrimitive(new LazyParsedNumber(number)));
    }

    @Override
    public void startObject() {
        values.offerLast(new JsonObject());
    }

    @Override
    public void endObject() {
        pre = values.pollLast();
    }

    @Override
    public void startArray() {
        values.offerLast(new JsonArray());
    }

    @Override
    public void endArray() {
        pre = values.pollLast();
    }

    @Override
    public void endObjectName(String name) {
        names.offerLast(name);
    }

    @Override
    public void endObjectValue() {
        if (!values.isEmpty()) {
            JsonValue value = (pre == null ? values.pollLast() : pre);
            String name = names.pollLast();

            JsonValue object = values.peekLast();
            if (value != null && object != null && name != null) {
                object.asObject().set(name, value);
            }
            pre = null;
        }
    }

    @Override
    public void endArrayElement() {
        if (!values.isEmpty()) {
            JsonValue ele = (pre == null ? values.pollLast() : pre);
            JsonValue array = values.peekLast();

            if (ele != null && array != null) {
                array.asArray().add(ele);
            }
            pre = null;
        }
    }


    @Override
    public JsonValue peekLast() {
        return values.peekLast();
    }

    @Override
    public JsonValue getRoot() {
        return pre;
    }

    @Override
    public int size() {
        return values.size();
    }
}