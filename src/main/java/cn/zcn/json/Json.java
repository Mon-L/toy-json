package cn.zcn.json;

import cn.zcn.json.ast.*;
import cn.zcn.json.stream.DefaultJsonVisitor;
import cn.zcn.json.stream.JsonReader;
import cn.zcn.json.stream.JsonReader2;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Convenient JSON read method.
 *
 * @author zicung
 */
public class Json {

    public static JsonValue read(String json) {
        return read(new StringReader(json));
    }

    public static JsonValue read(byte[] bytes) {
        return read(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
    }

    private static JsonValue read(Reader reader) {
        return new JsonReader(reader, new DefaultJsonVisitor()).read();
    }
}
