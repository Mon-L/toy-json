package cn.zcn.json.stream;

import cn.zcn.json.ast.*;

public class DefaultJsonVisitor extends JsonVisitor {

    private JsonValue current;

    @Override
    public void endNull() {
        this.current = JsonPrimitive.NULL;
    }

    @Override
    public void endBool(boolean bool) {
        this.current = bool ? JsonPrimitive.TRUE : JsonPrimitive.FALSE;
    }

    @Override
    public void endString(String string) {
        this.current = new JsonPrimitive(string);
    }

    @Override
    public void endNumber(String number) {
        this.current = new JsonPrimitive(new LazyParsedNumber(number));
    }

    @Override
    public JsonObject startObject() {
        return new JsonObject();
    }

    @Override
    public void endObject(JsonObject object) {
        this.current = object;
    }

    @Override
    public JsonArray startArray() {
        return new JsonArray();
    }

    @Override
    public void endArray(JsonArray array) {
        this.current = array;
    }

    @Override
    public void
    endObjectValue(JsonObject object, String name) {
        object.set(name, current);
    }

    @Override
    public void endArrayElement(JsonArray array) {
        array.add(current);
    }

    @Override
    public JsonValue getCurrent() {
        return current;
    }
}