CREATE SCHEMA config_manager;

CREATE TABLE config_info
(
    id           bigint       not null auto_increment,
    namespace_id varchar(128) not null,
    group_id     varchar(64)  not null,
    data_id      varchar(64)  not null,
    content      binary       not null,
    config_type  varchar(32)  not null default 'text',
    primary key (id),
    unique key (namespace_id, group_id, data_id)
);

CREATE TABLE config_info_beta
(
    id           bigint        not null auto_increment,
    namespace_id varchar(128)  not null,
    group_id     varchar(64)   not null,
    data_id      varchar(64)   not null,
    content      blob          not null,
    config_type  varchar(32)   not null default 'text',
    client_ips   varchar(1024) not null,
    primary key (id),
    unique key (namespace_id, group_id, data_id)
);

CREATE TABLE user
(
    id        int          not null auto_increment,
    user_name varchar(50)  not null,
    password  varchar(128) not null,
    primary key (id),
    unique key (user_name)
);

CREATE TABLE user_role
(
    id        bigint not null auto_increment,
    user_id   bigint not null,
    role_type int default 0,
    primary key (id)
);

CREATE TABLE namespace
(
    id             bigint        not null auto_increment,
    namespace_name varchar(1000) not null,
    namespace_id   varchar(255),
    primary key (id),
    unique key (namespace_id)
);

CREATE TABLE namespace_permissions
(
    id           bigint       not null auto_increment,
    user_id      bigint       not null,
    namespace_id varchar(255) not null,
    primary key (id)
);

INSERT INTO user(user_name, password)
VALUES ('lessSpring', '29591314');

INSERT INTO namespace(namespace_name, namespace_id)
VALUES ('default', 'default');