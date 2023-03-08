package cn.zcn.json.ast;

/**
 * @author zicung
 */
public class LazyParsedNumber extends Number {

    private final String val;
    private Integer integerVal;
    private Long longVal;
    private Float floatVal;
    private Double doubleVal;

    public LazyParsedNumber(String val) {
        this.val = val;
    }

    @Override
    public int intValue() {
        if (integerVal == null) {
            integerVal = Integer.parseInt(val);
        }

        return integerVal;
    }

    @Override
    public long longValue() {
        if (longVal == null) {
            longVal = Long.parseLong(val);
        }

        return longVal;
    }

    @Override
    public float floatValue() {
        if (floatVal == null) {
            floatVal = Float.parseFloat(val);
        }

        return floatVal;
    }

    @Override
    public double doubleValue() {
        if (doubleVal == null) {
            doubleVal = Double.parseDouble(val);
        }

        return doubleVal;
    }

    @Override
    public String toString() {
        return val;
    }
}
