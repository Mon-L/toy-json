package cn.zcn.json.ast;

/**
 * JSON AST Visitor
 *
 * @author zicung
 */
public class JsonVisitor {

    /**
     * 开始解析 JSON Literal {@code null}
     */
    public void startNull() {

    }

    /**
     * 解析 JSON Literal {@code null} 结束
     */
    public void endNull() {

    }

    /**
     * 开始解析 JSON Literal {@code Boolean}
     */
    public void startBool() {

    }

    /**
     * 解析 JSON Literal {@code Boolean} 结束
     *
     * @param bool 完成解析获得的 {@code Boolean}
     */
    public void endBool(boolean bool) {

    }

    /**
     * 开始解析 JSON Literal {@code Number}
     */
    public void startNumber() {

    }

    /**
     * 解析 JSON Literal {@code Number} 结束
     *
     * @param number 完成解析获得的 {@code Number}
     */
    public void endNumber(String number) {

    }

    /**
     * 开始解析 JSON Literal {@code String}
     */
    public void startString() {

    }

    /**
     * 解析 JSON Literal {@code String} 结束
     *
     * @param string 完成解析获得的 {@code String}
     */
    public void endString(String string) {

    }

    /**
     * 开始解析 JSON Object
     *
     * @return a new JsonObject
     */
    public JsonObject startObject() {
        return null;
    }

    /**
     * 解析 JSON Object 结束
     *
     * @param object 完成解析获得的 JSON Object
     */
    public void endObject(JsonObject object) {

    }

    /**
     * 开始解析 member name
     *
     * @param object 当前正在解析的 JSON Object
     */
    public void startObjectName(JsonObject object) {

    }

    /**
     * 解析 member name 结束
     *
     * @param object 当前正在解析的 JSON Object
     * @param name   完成解析获得的 member name
     */
    public void endObjectName(JsonObject object, String name) {

    }

    /**
     * 开始解析 member value
     *
     * @param object 当前正在解析的 JSON Object
     * @param name   当前正在解析的 member name
     */
    public void startObjectValue(JsonObject object, String name) {

    }

    /**
     * 解析 member value 结束
     *
     * @param object 当前正在解析的 JSON Object
     * @param name   当前正在解析的 member name
     */
    public void endObjectValue(JsonObject object, String name) {

    }

    /**
     * 开始解析 JSON Array
     *
     * @return a new JsonArray
     */
    public JsonArray startArray() {
        return null;
    }

    /**
     * 解析 JSON Array 结束
     *
     * @param array 解析完成的 JSON Array
     */
    public void endArray(JsonArray array) {

    }

    /**
     * 开始解析 element value
     *
     * @param array 当前正在解析的 JSON Array
     */
    public void startArrayElement(JsonArray array) {

    }

    /**
     * 解析 element value 结束
     *
     * @param array 当前正在解析的 JSON Array
     */
    public void endArrayElement(JsonArray array) {

    }

    public JsonValue getCurrent() {
        return null;
    }
}
