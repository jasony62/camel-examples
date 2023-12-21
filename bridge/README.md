示例桥接消息队列和 REST 接口。

通过消息队列进行实时数据交换是一种常用的方案。但是，很多系统是采用 webhook 方式（回调指定的 api）方式发送更新数据。这样就需要解决 api 和消息队列的互通问题。本示例演示了通过`Camel`连接 api 和消息队列的方法。

# 启动 kafka

本示例中使用`redpanda`替代`kafka`。

> Redpanda is a simple, powerful, and cost-efficient streaming data platform that is compatible with Kafka® APIs while eliminating Kafka complexity.

参考：https://redpanda.com

进入`docker`目录，启动服务

> docker-compose -f docker-compose.redpanda.yml up

`redpanda`自带 web 管理界面，可进入查看数据

> http://localhost:8066

# 运行

打包

```sh
mvn clean package
```

运行

```sh
java -Dcamel.springboot.routes-include-pattern='file:routes/*.xml' -jar ./target/bridge-1.0-SNAPSHOT.jar
```

发起调用。参数`messageId`会作为 kafka 消息的`key`，注意修改。

```sh
curl -v 'http://localhost:8081/bridge/bridge01?messageId=1' -H 'Content-Type: application/json'  -d '{"prop1":"value1","prop2":{"prop2_1":123}}'
```

返回结果。请查看`outbox/messages.jsonl`文件。

# 路由

## produce.xml

通过 api 接收数据。

```xml
<rest path="/bridge">
  <post path="/bridge01" consumes="application/json">
    <to uri="direct:bridge01" />
  </post>
</rest>
```

将收到的数据，发送到 kafka 队列中。

```xml
<!-- 将收到的消息发送到kafka -->
<setBody>
  <simple>${body}</simple>
</setBody>
<setHeader name="Kafka.KEY">
  <header>messageId</header>
</setHeader>
<to uri="kafka:camel-bridge?brokers=localhost:19092" />
```

发送后，可以在`redpanda`的管理应用中查看数据。

## consume.xml

从 kafka 接收数据，保存到本地文件。

从 kafka 接收数据。通过`Kafka.OFFSET`指定起始消费位置。通过`groupId`指定消费组名称。

```xml
<setHeader name="Kafka.OFFSET">
  <constant>0</constant>
</setHeader>
<from
  uri="kafka:camel-bridge?brokers=localhost:19092&amp;groupId=camel" />
```

显示收到的消息。

```xml
<log message="Message received from Kafka : ${body}" />
<log message="on the topic ${headers[kafka.TOPIC]}" />
<log message="on the partition ${headers[kafka.PARTITION]}" />
<log message="with the offset ${headers[kafka.OFFSET]}" />
<log message="with the key ${headers[kafka.KEY]}" />
```

将收到的消息。通过`fileExist=Append`设置为追加方式。通过`appendChars=\n`添加换行符。

```xml
<to uri="file:outbox/?fileName=messages.jsonl&amp;fileExist=Append&amp;appendChars=\n" />
```

# 参考

https://camel.apache.org/components/4.0.x/kafka-component.html
