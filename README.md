# TOY - JSON

基于 Java 的 JSON 语法解析器。该项目仅用于学习交流。

* JsonReader: 使用迭代的方式解析 JSON 。
* JsonReader2: 使用递归的方式解析 JSON。

## Read

``` java
JsonValue json = Json.read("{\"foo\": \"bar\"}");
JsonObject jsonObject = json.asObject();
```

## Write

``` java

//create json object
JsonObject json = new JsonObject();
json.set("foo", "bar");

//write json object
StringWriter writer = new StringWriter();
JsonWriter jsonWriter = new JsonWriter(writer)；
jsonWriter.write(json);

//get json
String jsonString = writer.toString();
```

## JSON forms

### Object
![object.png](resources%2Fobject.png)

### Array
![array.png](resources%2Farray.png)

### Value
![value.png](resources%2Fvalue.png)

### String
![string.png](resources%2Fstring.png)

### Number
![number.png](resources%2Fnumber.png)

### Whitespace
![whitesapce.png](resources%2Fwhitesapce.png)

## 参考

https://www.json.org/json-en.html

