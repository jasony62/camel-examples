示例处理 JSON 数据。

现在的 apis 基本上都是采用 JSON 格式传递数据，所以在 apis 编排时，就会涉及 JSON 数据适配的问题，就是将 A 接口返回的数据，改写为 B 接口需要的接入数据。`Camel`支持大量的数据格式转换，基本过程是收到数据后用`Unmarshal`组件把数据转换为 Java 对象，通过表达式语言处理数据，然后发送。在本示例中，数据改写通过`Mustache`组件完成，这样可以极大简化数据拼接工作。

# 运行

打包

```sh
mvn clean package
```

运行

```sh
java -Dcamel.springboot.routes-include-pattern='file:routes/*.xml' -jar ./target/adaption-1.0-SNAPSHOT.jar
```

发起调用

```sh
 curl -v 'http://localhost:8081/adaption/adapt01' -H 'Content-Type: application/json'  -d '{"prop1":"value1","prop2":{"prop2_1":123}}
```

返回结果

```json
{
  "key1": "value1",
  "key2": {
    "key2_1": 123
  }
}
```

# 路由

将收到的数据转换为 JSON 对象

```xml
<unmarshal>
  <json />
</unmarshal>
```

将 JSON 对象放到模板中生成文本。模板文件的位置通过`header`指定。

```xml
<setHeader name="MustacheResourceUri">
  <!-- 模板文件的路径 -->
  <constant>
    file:templates/template01.mustache</constant>
</setHeader>
```

模板文件（templates/template01.mustache）

```
{"key1":"{{body.prop1}}","key2":{"key2_1":{{body.prop2.prop2_1}} } }
```

**注意**：`Mustache`模板引擎是用`{{`和`}}`作为表达式的占位符，JSON 中也要用`{`和`}`，要避免冲突，例如：上例中的结尾部分。

# 参考

https://camel.apache.org/components/4.0.x/eips/marshal-eip.html

https://camel.apache.org/components/4.0.x/eips/unmarshal-eip.html

https://camel.apache.org/components/4.0.x/mustache-component.html

https://camel.apache.org/components/4.0.x/dataformats/jackson-dataformat.html

https://tomd.xyz/camel-transformation/
