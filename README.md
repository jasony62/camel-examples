`Apache Camel`是一个功能丰富的应用集成框架，可以方便的实现系统间的互通，它提供了应用编排能力。

本项目出于学习和验证的目标，针对一些典型的应用集成的场景编写示例，

# Camel 基础知识

`Camel`虽然比较容易上手，但是整体上比较复杂，功能非常丰富，官网的在线文档也非常多，下面列出了一些最基础的概念和相应的在线文档链接。

## 路由

`Camel`最核心的功能是路由，并且支持用 java，xml 和 yaml 格式编写路由。

```xml
<?xml version="1.0" encoding="UTF-8"?>

<routes xmlns="http://camel.apache.org/schema/spring">
  <route>
    <from uri="timer:xml?repeatCount=1" />
    <setBody>
      <simple>Welcome Camel from ${routeId}</simple>
    </setBody>
    <log message="${body}" />
  </route>

</routes>
```

掌握路由的编写主要是学习`EIP(Enterprise Integration Patterns)`，其中说明了路由中的各种组件。

参考：

https://camel.apache.org/components/4.0.x/others/java-xml-io-dsl.html

https://camel.apache.org/components/4.0.x/eips/enterprise-integration-patterns.html

https://camel.apache.org/manual/routes.html

https://camel.apache.org/manual/using-propertyplaceholder.html

## 表达式

`Camel`使用表达式在路由中支持更复杂的处理逻辑。`simple`是最基础的表达式语言。

参考：

https://camel.apache.org/components/4.0.x/languages/simple-language.html

## REST

`Camel`支持在路由中配置 REST 端点（endpoint），实现通过 http 调用路由。

参考：

https://camel.apache.org/manual/rest-dsl.html

## 功能扩展

`Camel`在路由的基础上支持进行功能扩展，最常用的是自定义`processor`。简单的说，`processor`就是用于实现自定义的数据处理逻辑。

参考：

https://camel.apache.org/manual/processor.html

# Camel 日常操作

创建`Springboot`项目

```sh
mvn -X archetype:generate \
  -DarchetypeGroupId=org.apache.camel.archetypes \
  -DarchetypeArtifactId=camel-archetype-spring-boot \
  -DarchetypeVersion=4.2.0
```

打包

```sh
mvn clean package
```

运行

```sh
java -Dcamel.springboot.routes-include-pattern='file:routes/*.xml,classpath:camel/*.xml' -jar ./camel-demo01-1.0-SNAPSHOT.jar
```

参数`camel.springboot.routes-include-pattern`用于指定路由文件的位置。

# 示例

| 名称       | 功能描述                                           | 文档                          |
| ---------- | -------------------------------------------------- | ----------------------------- |
| fileserver | 文件服务器，支持通过 http 上传文件，并保存在本地。 | [说明](/fileserver/README.md) |
| apiflow    | 演示 apis 编排。                                   | [说明](/apiflow/README.md)    |

# docker 中运行

在项目根目录下，以`fileserver`为例

```sh
docker run -it --rm --name camel-fileserver --workdir /home -v $PWD/fileserver/routes:/home/routes -p 8085:8081 openjdk:17-alpine sh
```

```sh
docker cp $PWD/fileserver/target/fileserver-1.0-SNAPSHOT.jar camel-fileserver:/home/fileserver-1.0-SNAPSHOT.jar
```

在容器中运行

```sh
java -Dcamel.springboot.routes-include-pattern='file:routes/*.xml' -jar ./fileserver-1.0-SNAPSHOT.jar
```
