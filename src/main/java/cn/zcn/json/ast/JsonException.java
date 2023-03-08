package cn.zcn.json.ast;

/**
 * @author zicung
 */
public class JsonException extends RuntimeException {

    public JsonException(String msg) {
        super(msg);
    }

    public JsonException(String msg, Throwable t) {
        super(msg, t);
    }
}
