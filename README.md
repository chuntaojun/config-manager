# config-manager（目前为个人毕设项目）

#### LessSpring ConfigManager

在造的一款基于*raft+h2*的分布式配置管理组件，不再需要依赖额外的数据库组件，完全依靠*Raft*协议来实现集群间的数据同步

#### 项目依赖

 - SpringBoot WebFlux
 - Lombok
 - OkHttp
 - JRaft
 - Guava
 - H2DataBase
 - Prometheus

#### 项目进展

 - [x] 本地配置优先原则
 - [x] 本地容灾措施
 - [x] *Raft*协议集成H2（初步完成，Multi-Group部分待研究）
 - [x] 简单权限（暂未实现资源的权限设置）
 - [x] 采取*nacos*的文件缓存理念
 - [x] 配置文件加解密实现
 - [ ] 配置的灰度发布（目前参考*nacos*的基于*IP*实现）
 - [x] 配置变动监听功能
 - [ ] 前端页面、可视化管理
 - [ ] 数据分片存储（摆脱单机存储的限制）
 - [ ] 支持*prometheus*数据监控
 - [ ] 详细的日志输出分类
 - [ ] 邮件通知系统

#### 使用说明

##### 项目相关文件路径说明

> config-manager-client



> config-manager-server



##### 项目使用端口

> server-port=2959 config-manager-server的监听端口

> raft-server-port=3959 raft-group 集群的监听端口

二者关系：`{raft-server-port}={server-port}+1000`

##### 系统参数说明

> Config-Manager-Client 端参数信息

> Config-Manager-Server 端参数信息

###### application.properties

```properties
# config-manager-server 运行模式，单机——standalone，集群——cluster，默认以单机模式启动
com.lessspring.org.config-manager.server.mode=standalone
# 缓存类型，inner为内部guava的Cache实现，redis，默认为inner
com.lessspring.org.config-manager.cache.type=inner
# 运行环境类型，设置为develop时，则不会开启权限验证
com.lessspring.org.config-manager.environment=develop
# 设置并发qps，根据资源名称 resource-name 进行限制 qps
com.lessspring.org.config-manager.tps.resources[0].resource-name=
com.lessspring.org.config-manager.tps.resources[0].qps=
# JWT有效期
com.lessspring.org.config-manger.jwt.survival.time.second=
# JWT的签名信息
com.lessspring.org.config-manger.jwt.signature=
# JWT算法密钥
com.lessspring.org.config-manager.security.jwt.secret=
# config-manager-server的相关文件存在位置
com.lessspring.org.config.manager.cache-dir=${user.home}/config-manager/server-${server.port}
# api白名单设置
com.lessspring.org.config-manager.anyuri=/, /api/v1/login, /api/v1/cluster/all
# 设置集群选主超时时间
com.lessspring.org.config-manager.raft.electionTimeoutMs=
# 设置raft快照任务间隔时间
com.lessspring.org.config-manager.raft.snapshotIntervalSecs=

#
com.lessspring.org.config-manager.email.host=
#
com.lessspring.org.config-manager.email.username=
#
com.lessspring.org.config-manager.email.password=
#
com.lessspring.org.config-manager.email.smtp.auth=
#
com.lessspring.org.config-manager.starttls.enable=
#
com.lessspring.org.config-manager.email.starttls.required=
```

###### cluster.properties

```properties
# 本机的ip index 序号
cluster.server.node.self.index={num}
# 机器 num 的 ip 信息
cluster.server.node.ip.{num}=127.0.0.1
# 机器 num 的 port 信息
cluster.server.node.port.{num}=2959
```

##### key 的构建说明

> config-manager-client


> config-manager-server




