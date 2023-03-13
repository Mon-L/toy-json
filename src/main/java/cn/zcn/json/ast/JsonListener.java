package cn.zcn.json.ast;

/**
 * JSON AST Listener
 *
 * @author zicung
 */
public class JsonListener {

    /**
     * 开始解析 Literal {@code null}
     */
    public void startNull() {

    }

    /**
     * 解析 Literal {@code null} 结束
     */
    public void endNull() {

    }

    /**
     * 开始解析 Literal {@code Boolean}
     */
    public void startBool() {

    }

    /**
     * 解析 Literal {@code Boolean} 结束
     *
     * @param bool 完成解析获得的 {@code Boolean}
     */
    public void endBool(boolean bool) {

    }

    /**
     * 开始解析 Literal {@code Number}
     */
    public void startNumber() {

    }

    /**
     * 解析 Literal {@code Number} 结束
     *
     * @param number 完成解析获得的 {@code Number}
     */
    public void endNumber(String number) {

    }

    /**
     * 开始解析 Literal {@code String}
     */
    public void startString() {

    }

    /**
     * 解析 Literal {@code String} 结束
     *
     * @param string 完成解析获得的 {@code String}
     */
    public void endString(String string) {

    }

    /**
     * 开始解析 JSON Object
     */
    public void startObject() {

    }

    /**
     * 解析 JSON Object 结束
     */
    public void endObject() {

    }

    /**
     * 开始解析 member name
     */
    public void startObjectName() {

    }

    /**
     * 解析 member name 结束
     *
     * @param name 完成解析获得的 member name
     */
    public void endObjectName(String name) {

    }

    /**
     * 开始解析 member value
     */
    public void startObjectValue() {

    }

    /**
     * 解析 member value 结束
     */
    public void endObjectValue() {

    }

    /**
     * 开始解析 JSON Array
     */
    public void startArray() {

    }

    /**
     * 解析 JSON Array 结束
     */
    public void endArray() {

    }

    /**
     * 开始解析 element value
     */
    public void startArrayElement() {

    }

    /**
     * 解析 element value 结束
     */
    public void endArrayElement() {

    }

    public JsonValue getRoot() {
        return null;
    }

    public JsonValue peekLast() {
        return null;
    }

    public int size() {
        return 0;
    }
}
