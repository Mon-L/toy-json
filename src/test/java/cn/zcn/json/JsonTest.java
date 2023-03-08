package cn.zcn.json;

import cn.zcn.json.ast.JsonException;
import cn.zcn.json.ast.JsonObject;
import cn.zcn.json.ast.JsonValue;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author zicung
 */
public class JsonTest {

    @Test
    public void testRead() {
        JsonValue json = Json.read("{\"id-abcdefghijklmnopqrstuvwxyz\": \"auth_sbox\", \"isTrue\":true,  \"isFalse\":false,\"isNull\":null,  \"age\":{\"a\":14982},\"isArray\" :  [1,\"234\", true,false,  {\"a\":\"b\"}]}");
        assertThat(json).isInstanceOf(JsonObject.class);
    }

    @Test
    public void testReadEmptyString() {
        assertThatExceptionOfType(JsonException.class).isThrownBy(() -> Json.read(""));
    }

    @Test
    public void readInvalidEndingJson() {
        assertThatExceptionOfType(JsonException.class).isThrownBy(() -> Json.read("{\"a\":\"b\"} 123"));
        assertThatExceptionOfType(JsonException.class).isThrownBy(() -> Json.read("{\"a\":\"b\"} {\"c\":\"d\"}"));
    }

    @Test
    public void readInvalidBeginningJson() {
        assertThatExceptionOfType(JsonException.class).isThrownBy(() -> Json.read("123 {\"a\":\"b\"}"));
    }
}
