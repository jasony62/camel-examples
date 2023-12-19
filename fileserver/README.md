示例通过 http 表单上传文件场景。

# 运行

## 打包

```sh
mvn clean package
```

## 运行

```sh
java -Dcamel.springboot.routes-include-pattern='file:routes/*.xml' -Dfileserver.port=8085 -jar ./target/fileserver-1.0-SNAPSHOT.jar
```

## 上传文件

```sh
curl -v -X POST -F 'uploaded=@文件路径' -F 'fields={"abc":123,"xyz":"hello"}' -F key1=value1 -F key2=value2 'http://localhost:8085/upload?prefix=文件保存目录'
```

# 代码

示例中有两个自定义的`processor`：UploadProcessor 和 FileProcessor。

## UploadProcessor

UploadProcessor 用于对整个文件上传请求进行处理，用于提取请求中上传文件的数量和在表单中的字段名。执行完成后，在`exchange`的属性中：1、添加`filePartNames`，用于保存上传文件在表单中的名称列表；2、添加`fieldPartNames`，用于保存表单中非文件字段名称。

`camel-jetty`组件默认支持处理`multipart/form-data`内容，会将表单中的每一个分段转换一个`attachment`对象。

```java
AttachmentMessage am = exchange.getMessage(AttachmentMessage.class);
Map<String, Attachment> aos = am.getAttachmentObjects();
```

表单中的每个分段都有`content-disposition`头，如果其中有`filename`，就是文件。

```java
if (hname.equals("content-disposition")) {
  if (hvalue.contains("filename=")) {
    filePartNames.add(partName);
  } else {
    fieldPartNames.add(partName);
  }
}
```

## FileProcessor

FileProcessor 用于将单个文件的放到`messsage.body`中，和提取文件名称。

```java
DataHandler dh = attachment.getDataHandler();
InputStream is = dh.getInputStream();
exchange.getIn().setBody(is);
```

处理的文件的文件名称保存在`exchange`的属性`processedFilename`中。

```java
exchange.setProperty(processedFilenamePropKey, filename);
```

# 路由

## 声明 processor

```xml
<bean name="uploadProcessor" type="jasony62.camel.UploadProcessor"></bean>
<bean name="fileProcessor" type="jasony62.camel.FileProcessor" />
```

如果需要可以进行 bean 的初始化。

```xml
<bean name="fileProcessor" type="jasony62.camel.FileProcessor">
  <properties>
    <property key="processedFilenamePropKey" value="processedFilename" />
  </properties>
</bean>
```

## REST 端点

```xml
<from uri="jetty:http://0.0.0.0:{{fileserver.port}}/upload" />
```

定义 http 接口，其中`{{fileserver.port}}`是系统参数，在`application.properties`中指定，默认值 8081，也可以在运行时通过`-Dfileserver.port=xxxx`指定。

参考：

https://camel.apache.org/manual/using-propertyplaceholder.html

## 使用 UploadProcessor

```xml
 <process ref="uploadProcessor" />
```

可以使用`log`组件输出调试信息。

```xml
<log message="文件数量：${exchangeProperty.filePartNames.size()}" />
<log message="字段数量：${exchangeProperty.fieldPartNames.size()}" />
```

## 处理每个文件

```xml
<loop>
  <!--设置循环次数-->
  <simple>${exchangeProperty.filePartNames.size()}</simple>
  <!--给processor设置参数-->
  <bean ref="fileProcessor"
    method="setFilePartName(${exchangeProperty.filePartNames[${header.CamelLoopIndex}]})" />
  <!--处理文件-->
  <process ref="fileProcessor" />
  <!--保存到本地文件-->
  <to
    uri="file:outbox/?fileName=${header.dir}${exchangeProperty.processedFilename}"></to>
</loop>
```

**注意**：上传文件请求中的查询参数和表单中的非文件字段都放到`message.header`中。本示例是在查询参数中指定了`prefix`，如果`prefix`是目录，应该以`/`结尾。

## 清空消息内容

```xml
<transform>
  <constant></constant>
</transform>
```

## 将表单中所有非文件字段保存到一个文件

```xml
<loop>
  <simple>${exchangeProperty.fieldPartNames.size()}</simple>
  <transform>
    <simple>${body}${exchangeProperty.fieldPartNames[${header.CamelLoopIndex}]}=${header[${exchangeProperty.fieldPartNames[${header.CamelLoopIndex}]}]}\n</simple>
  </transform>
</loop>
<to uri="file:outbox/?fileName=${header.prefix}fields.txt"></to>
```

## 设置请求响应内容

```xml
<route>
  <from uri="direct:response" />
  <setBody>
    <simple>ok</simple>
  </setBody>
</route>
```
