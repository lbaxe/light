在北纬38°艰苦奋斗，认真搬砖。

如果这个框架让你有所收获，记得 Star 关注哦，这对我很重要。

# 新手必读

* 文档：https://www.lbaxe.com/docs/light
* 示例工程：

# light框架简介

&emsp;&emsp;为单应用提供的基本 `SpringMVC` 轻框架，基于 `SpringBoot` 进行 `mvc` 的一些扩展，和自动配置，使开发尽量简单。 无配置中心的，基于本地文件配置的 `RPC` 服务

| 基座 | 版本 |
| --- | --- |
| SpringBoot | 2.7.18 |
| SpringFramework | 5.3.32 |
| JDK | 8 |
| Tomcat | 9 |
| Java/Jakarta EE|JavaEE7-8|

版本选择说明，详情参考：https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-Versions

* `SpringFramework5` 是最后一个支持 `JavaEE 7-8 (javax namespace)` 规范的版本，JavaEE 7-8对应的servlet规范包名空间依然javax，从9开始 `JavaEE`
  正式更名为 `JakataEE9`，且servlet规范包名空间改为 (jakarta namespace)。 同时SpringFramework5支持JDK 8-21版本范围
* `SpringBoot2.x` 是支持 `JDK8` 的最高版本，而`SpringBoot3.x` 是最低支持版本 `JDK17`，不再支持 `JDK8`
* `Tomcat9` 是最后一个支持JavaEE8的版本，最低支持 `JDK8`，正因如此，`SpringBoot2.x` 内嵌容器选择了 `Tomcat9`
* `JDK8` 如果使用 `Oracle` 的 `JDK`，建议最高版本 `OracleJDK8u202`，因为之后的版本版权有变化，规避不必要的风险

# 框架简介

提供了以下的基本配置和功能支持

## 组件关系

![light框架](https://gitee.com/lbaxe/assets/raw/master/note-image/light.png)

## 基本功能

### light-framework

* 规范目录
* json返回值统一处理：`ResponseBody` 返回值为 `AjaxResult`
* `Controller` 层统一异常处理
* `Filter` 层统一异常处理：基于 `forword` 重定向
* 统一异常信息管理：基于枚举
* 日志目录分离：集成 `log4j2`，并支持自定义日志目录
* 防xss脚本攻击：支持注解逃逸
* `Redis`缓存：支持原生命令级对应的接口方法调用及调用日志监控
* 本地缓存：支持基于 ehcache 的本地缓存

### light-mapper

* 实体层的表级缓存：通过去除 `MybatisPlusAutoConfiguration` 自动配置类，定制 `MybatisPlus` 配置，实现表级一级缓存
* 数据源分片：通过 `ShardingJDBC` 数据源集成，去除 `SpringBootConfiguration` 自动配置类，改为依赖配置文件属性启动
* sql简化：基于 `MybatisPlus`，扩展了 BaseMapper 通用方法，避免基础sql的编写
* 数据库字段加密：自动加解密支持

### light-common

* `ip` 逆地址解析：基于 `ip` 文件库的 `ip` 行政区域查询
* `GEO`: 各地图坐标系转换工具
* `httpclient` 简化：基于Apache httpclient4.0+异步客户端工具封装
* 加解密：线程安全的AES与3DES加密算法工具封装
* excel处理：基于easyexcel3工具封装
* 链路追踪：链路追踪的基本支持

# 技术栈

| 组件或框架 | 说明 | 版本 | 官方文档 |
| --- | --- | --- | --- |
| SpringBoot | Spring应用启动框架 | 2.7.18 | [文档](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/) |
| SpringFramework | Java MVC应用开发框架 | 5.3.32 | [文档](https://docs.spring.io/spring-framework/docs/5.3.37/reference/html/) |
| MySQL | 数据库 |5.7.31 | [文档](https://dev.mysql.com/doc/refman/5.7/en/) |
| MyBatis | ORM框架 |3.5.7 | [文档](https://mybatis.org/mybatis-3/zh_CN/index.html) |
| MyBatisPlus| MyBatis增强包 |3.4.2 | [文档](https://baomidou.com/) |
| Redis | kv数据库 |6.2.14 | [文档](https://redis.io/docs/latest/develop/) |
| Lettuce | Redis客户端 |6.1.10.RELEASE | [文档](https://redis.io/docs/latest/develop/connect/clients/java/lettuce/) |
| Spring Security | Spring 安全框架 | 5.7.11 | [文档](https://docs.spring.io/spring-security/reference/5.7/servlet/getting-started.html) |
| Hibernate Validator | 参数校验组件 | 6.2.5 | [文档](https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/html_single/) |
| Quartz | 任务调度组件 | 2.3.2 | [文档](https://github.com/quartz-scheduler/quartz/wiki) |
| Lombok | 消除冗长的 Java 代码 | 1.18.30 | [文档](https://projectlombok.org/) |

# 使用框架的典型配置

```yaml
server:
  port: 8088
spring:
  application:
    name: light-test
  redis:
    host: 192.168.0.100
    port: 6379
    password: 123456
  datasource:
    dynamic:
      #设置严格模式，默认false不启动，不启动则使用默认数据源;启动后未匹配到指定数据源时会抛出异常
      strict: false
      #设置默认数据源，非默认数据源DAO层类需要加注解@DS("数据库名")
      primary: master
      datasource:
        master:
          username: root
          password: 123456
          url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&tinyInt1isBit=false
          driver-class-name: com.mysql.cj.jdbc.Driver
          type: com.zaxxer.hikari.HikariDataSource
        test:
          username: root
          password: 123456
          url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&tinyInt1isBit=false
          driver-class-name: com.mysql.cj.jdbc.Driver
          type: com.zaxxer.hikari.HikariDataSource

  # 分库分表
  shardingsphere:
    enabled: true
    #数据源信息
    datasource:
      names: sharding-test
      sharding-test:
        username: root
        password: 123456
        jdbc-url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&tinyInt1isBit=false
        driver-class-name: com.mysql.cj.jdbc.Driver
        type: com.zaxxer.hikari.HikariDataSource
      #属性配置(可选)
    props:
      #是否开启SQL显示，默认值: false
      sql.show: true
      #executor.size: 工作线程数量，默认值: CPU核数
    sharding:
      tables:
        device_dust_data:
          actual-data-nodes: sharding-test.user_$->{0..9}
          table-strategy:
            inline:
              algorithm-expression: user_$->{user_id % 10}
              sharding-column: user_id
  #spring security
  security:
    oauth2:
      client:
        registration:
          gitee:
            clientId: xxx
            clientSecret: xxx
            redirectUri: http://localhost:8080/oauth2/code/{registrationId}
            authorizationGrantType: authorization_code
          light-test:
            clientId: xxx
            clientSecret: xxx
            redirectUri: http://localhost:8080/oauth2/code/{registrationId}
            authorizationGrantType: client_credentials
        provider:
          gitee:
            authorizationUri: https://gitee.com/oauth/authorize
            tokenUri: https://gitee.com/oauth/token
            userInfoUri: https://gitee.com/api/v5/user
            userNameAttribute: name
          light-test:
            authorizationUri: https://gitee.com/oauth/authorize
            tokenUri: https://gitee.com/oauth/token
            userInfoUri: https://gitee.com/api/v5/user
            userNameAttribute: name
feign:
  oauth2:
    enabled: true
    clientRegistrationId: gitee
security:
  ignore:
    whites:
      - /uplod
      - /image
```

