package cn.zcn.json.ast;

public class UnexpectedException extends JsonException {
    public UnexpectedException(char expected, int actual, int line, int column) {
        super("Expected \"" + expected +
                "\" but got " + (actual == -1 ? "\"EOF\"" : (char) actual) +
                ".Line: " + line + ", Column: " + column);
    }
}