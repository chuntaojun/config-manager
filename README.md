# config-manager

#### LessSpring ConfigManager

在造的一款基于*raft+h2*的分布式配置管理组件，不再需要依赖额外的数据库组件，完全依靠*Raft*协议来实现集群间的数据同步

#### 项目依赖

 - SpringBoot WebFlux
 - Lombok
 - OkHttp
 - JRaft
 - H2DataBase

#### 项目进展

 - [ ] 本地配置优先原则
 - [x] 本地容灾措施
 - [x] Raft协议集成H2（初步完成，快照部分待研究）
 - [x] 简单权限（暂未实现资源的权限设置）
 - [ ] 采取`nacos`的文件缓存理念
 - [x] 配置文件加解密实现
 - [x] 配置变动监听功能
 - [ ] 前端页面、可视化管理
 - [ ] 数据分片存储（摆脱单机存储的限制）

#### 使用说明

##### 系统参数说明


