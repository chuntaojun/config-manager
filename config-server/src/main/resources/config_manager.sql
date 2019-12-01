create SCHEMA config_manager;

create TABLE config_info
(
    id           bigint       not null auto_increment,
    namespace_id varchar(128) not null,
    group_id     varchar(64)  not null,
    data_id      varchar(64)  not null,
    content      binary,
    config_type  varchar(32)  not null default 'text',
    file_source  binary,
    encryption   varchar(128) not null default '',
    create_time  bigint       not null,
    version      bigint       not null default 0,
    primary key (id),
    unique key (namespace_id, group_id, data_id)
);

create TABLE config_info_beta
(
    id           bigint        not null auto_increment,
    namespace_id varchar(128)  not null,
    group_id     varchar(64)   not null,
    data_id      varchar(64)   not null,
    content      binary        not null,
    config_type  varchar(32)   not null default 'text',
    file_source  binary,
    encryption   varchar(128)  not null default '',
    create_time  bigint        not null,
    client_ips   varchar(1024) not null,
    version      bigint       not null default 0,
    primary key (id),
    unique key (namespace_id, group_id, data_id)
);

create TABLE config_info_history
(
    id           bigint        not null auto_increment,
    namespace_id varchar(128)  not null,
    group_id     varchar(64)   not null,
    data_id      varchar(64)   not null,
    content      binary        not null,
    config_type  varchar(32)   not null default 'text',
    file_source  binary,
    encryption   varchar(128)  not null default '',
    create_time  bigint        not null,
    last_modify_time bigint    not null,
    primary key (id),
    unique key (namespace_id, group_id, data_id, last_modify_time)
);

create TABLE kms_secret_key(
    id          bigint         not null auto_increment,
    namespace_id    varchar(128)    not null,
    secret_key      varchar(2048)   not null,
    primary key(id),
    unique key(secret_key)
)

create TABLE user
(
    id        int          not null auto_increment,
    user_name varchar(50)  not null,
    password  varchar(128) not null,
    primary key (id),
    unique key (user_name)
);

create TABLE user_role
(
    id        bigint not null auto_increment,
    user_id   bigint not null,
    role_type int default 0,
    primary key (id)
);

create TABLE namespace
(
    id             bigint        not null auto_increment,
    namespace_name varchar(1000) not null,
    namespace_id   varchar(255),
    primary key (id),
    unique key (namespace_id)
);

create TABLE namespace_permissions
(
    id           bigint       not null auto_increment,
    user_id      bigint       not null,
    namespace_id varchar(255) not null,
    primary key (id)
);

insert into user(user_name, password)
VALUES ('lessSpring', '29591314');

insert into namespace(namespace_name, namespace_id)
values ('default', 'default');