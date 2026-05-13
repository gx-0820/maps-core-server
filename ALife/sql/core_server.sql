-- 切换到数据库：core_server

use `core_server`;

create table algorithm_data_fusion
(
    id           bigint auto_increment
        primary key,
    target_id    int                                 not null,
    longitude    double                              null,
    latitude     double                              null,
    altitude     double                              null,
    velocity     double                              null,
    azimuth      double                              null,
    type         varchar(50)                         null,
    name         varchar(100)                        null,
    distance     double                              null,
    pitch        double                              null,
    threat_level varchar(20)                         null,
    pan_angle    double                              null,
    tilt_angle   double                              null,
    zoom_level   double                              null,
    color        varchar(10)                         null,
    created_at   timestamp default CURRENT_TIMESTAMP null
);

create index idx_created_at
    on algorithm_data_fusion (created_at);

create table algorithm_geo_position_validator
(
    id            bigint auto_increment
        primary key,
    longitude     double                              null,
    latitude      double                              null,
    altitude      double                              null,
    warning_level int                                 not null,
    created_at    timestamp default CURRENT_TIMESTAMP null
);

create index idx_created_at
    on algorithm_geo_position_validator (created_at);

create table algorithm_object_detection
(
    id            bigint auto_increment
        primary key,
    target_left   int                                 not null,
    target_top    int                                 not null,
    target_width  int                                 not null,
    target_height int                                 not null,
    created_at    timestamp default CURRENT_TIMESTAMP null
);

create table algorithm_track_prediction
(
    id              bigint auto_increment
        primary key,
    target_id       bigint                              not null,
    latitude        double                              not null,
    longitude       double                              not null,
    altitude        double                              not null,
    sequence_number int                                 not null,
    created_at      timestamp default CURRENT_TIMESTAMP null
);

create index idx_created_at
    on algorithm_track_prediction (created_at);

create index idx_target_id
    on algorithm_track_prediction (target_id);

create table daily_drone_data
(
    id                   bigint auto_increment
        primary key,
    date                 date          not null,
    drone_count          int default 0 not null,
    illegal_drone_count  int default 0 not null,
    disposed_drone_count int default 0 not null,
    created_at           datetime      not null,
    updated_at           datetime      not null,
    constraint date
        unique (date)
);

create table device_electric_investigation_data
(
    id            bigint auto_increment
        primary key,
    original_json text         null,
    timestamp     datetime(6)  null,
    data_type     varchar(255) null,
    update_time   datetime(6)  null
);

create table device_photoelectric_data
(
    id            bigint auto_increment
        primary key,
    device_id     varchar(255) null,
    original_json text         null,
    timestamp     datetime(6)  null,
    data_type     varchar(255) null
);

create table device_radar_data
(
    id            bigint auto_increment
        primary key,
    device_id     varchar(255) null,
    original_json text         null,
    targets_json  text         null,
    timestamp     datetime(6)  null,
    data_type     varchar(255) null
);

create table device_video_frame
(
    id          bigint auto_increment
        primary key,
    device_id   varchar(255) null,
    frame_data  longblob     null,
    height      int          null,
    source_type varchar(255) null,
    timestamp   datetime(6)  null,
    width       int          null
);

create table exception_log
(
    id             int auto_increment comment '主键 id'
        primary key,
    opt_uri        varchar(255) not null comment '操作 URI',
    opt_method     varchar(255) not null comment '操作方法',
    request_method varchar(255) null comment '请求方法：Get，Post，Delete，Put',
    request_param  varchar(255) null comment '请求参数',
    opt_desc       varchar(255) null comment '操作描述',
    exception_info text         null comment '错误信息',
    ip_address     varchar(255) null comment 'ip 地址',
    ip_source      varchar(255) null comment 'ip 来源'
);

create table geofence
(
    id             bigint auto_increment
        primary key,
    name           varchar(255)   not null comment '禁飞区名称',
    core_longitude decimal(10, 7) not null comment '核心区经度(-180.0~180.0)',
    core_latitude  decimal(10, 7) not null comment '核心区纬度(-90.0~90.0)',
    core_radius    decimal(12, 2) not null comment '核心区半径(>0)',
    buffer_radius  decimal(12, 2) not null comment '缓冲区半径(>core_radius)',
    alert_radius   decimal(12, 2) not null comment '告警区半径(>buffer_radius)',
    constraint chk_radius
        check ((`core_radius` > 0) and (`buffer_radius` > `core_radius`) and (`alert_radius` > `buffer_radius`))
);

create index idx_coordinate
    on geofence (core_longitude, core_latitude);

create table monthly_drone_data
(
    id                   bigint auto_increment
        primary key,
    created_at           datetime(6)  not null,
    disposed_drone_count int          not null,
    drone_count          int          not null,
    illegal_drone_count  int          not null,
    timestamp            datetime(6)  null,
    updated_at           datetime(6)  not null,
    `year_month`         varchar(255) not null,
    constraint UK95w2fe5ah738scevnpro67tdx
        unique (`year_month`)
);

create table operation_log
(
    id             int auto_increment comment '主键 id'
        primary key,
    opt_module     varchar(45)  null comment '操作模块',
    opt_uri        varchar(255) null comment '操作 URI',
    opt_type       varchar(45)  null comment '操作类型：新增、修改等',
    opt_method     varchar(255) null comment '操作方法',
    opt_desc       varchar(255) null comment '操作描述',
    request_method varchar(255) null comment '请求方法：GET，POST，DELETE，PUT',
    request_param  text         null comment '请求参数',
    response_data  text         null comment '返回数据',
    user_id        int          null comment '操作用户 id',
    nickname       varchar(50)  null comment '操作用户昵称',
    ip_address     varchar(255) null comment '操作用户 id 地址',
    ip_source      varchar(255) null comment '操作用户 ip 来源'
);

create table permission
(
    id              int auto_increment
        primary key,
    permission_name varchar(255)         not null,
    permission_code varchar(255)         not null,
    status          tinyint(1) default 0 null,
    create_time     datetime             null,
    deleted         tinyint(1)           null
)
    row_format = DYNAMIC;

create table role
(
    id          int auto_increment
        primary key,
    role_name   varchar(255) not null,
    role_key    varchar(255) null comment '角色标识如ADMIN',
    status      tinyint(1)   null comment '0-正常 1-禁用',
    create_time datetime     null,
    deleted     tinyint(1)   null comment '0-未删除 1-已删除'
)
    row_format = DYNAMIC;

create table role_permission
(
    id            int auto_increment
        primary key,
    role_id       int not null,
    permission_id int not null,
    constraint role_permission_permission_fk2
        foreign key (permission_id) references permission (id),
    constraint role_permission_role_fk
        foreign key (role_id) references role (id)
            on delete cascade
)
    row_format = DYNAMIC;

create table user
(
    id          int auto_increment comment 'id'
        primary key,
    username    varchar(255)         not null,
    password    varchar(255)         not null,
    status      tinyint(1) default 0 not null comment '0-正常 1-禁用',
    create_time datetime             null,
    deleted     tinyint(1) default 0 null
)
    row_format = DYNAMIC;

create table user_role
(
    id      int auto_increment
        primary key,
    user_id int not null,
    role_id int not null,
    constraint user_role_role_fk
        foreign key (role_id) references role (id),
    constraint user_role_user_fk
        foreign key (user_id) references user (id)
)
    row_format = DYNAMIC;

create table config
(
    config_id    int auto_increment comment '配置ID'
        primary key,
    config_name  varchar(255)         not null comment '配置名称',
    config_key   varchar(255)         not null comment '配置键',
    config_value text                 null comment '配置值',
    config_type  varchar(10) default 'Y' null comment '配置类型：Y/N',
    create_by    varchar(50)          null comment '创建人',
    create_time  datetime             null comment '创建时间',
    update_by    varchar(50)          null comment '更新人',
    update_time  datetime             null comment '更新时间',
    remark       varchar(500)         null comment '备注',
    constraint uk_config_key
        unique (config_key)
) row_format = DYNAMIC;

create index idx_config_key
    on config (config_key);

-- 初始化OFD配置数据
insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time)
values (101, '目标距离偏差', 'sys.OFD.rangeDDeviation', '0', 'Y', 'admin', now());

insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time)
values (102, '目标方位角偏差', 'sys.OFD.azimuthDeviation', '0', 'Y', 'admin', now());

insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time)
values (103, '目标俯仰角偏差', 'sys.OFD.elevationDeviation', '0', 'Y', 'admin', now());

insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
values (112, '自动处置模式', 'sys.countermeasure.auto_mode', 'false', 'Y', 'admin', now(), 'true=自动 false=人工');

insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
values (113, '自动处置扫描周期毫秒', 'sys.countermeasure.scan_period_ms', '1000', 'Y', 'admin', now(), '自动处置轮次周期，单位毫秒');

insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
values (114, '自动处置策略配置', 'sys.countermeasure.strategy_profile',
        '{"activePreset":"A","presets":{"A":{"mode":"FIXED","actions":{"LOW":["NO_ACTION"],"MEDIUM":["UAV_ATTACK_AUTO"],"HIGH":["DECEPTION_DRIVE"]},"rules":{}},"B":{"mode":"FIXED","actions":{"LOW":["UAV_ATTACK_AUTO"],"MEDIUM":["DECEPTION_DRIVE"],"HIGH":["DECEPTION_CAPTURE"]},"rules":{}},"C":{"mode":"ADAPTIVE","actions":{"LOW":["DECEPTION_DRIVE"],"MEDIUM":["DECEPTION_DRIVE"],"HIGH":["DECEPTION_DRIVE"]},"rules":{"MULTI_TARGET":{"enabled":true,"priority":1,"condition":{"targetCountGte":2},"actions":["UAV_ATTACK_AUTO"]},"HIGH_DOMINANCE_UPGRADE":{"enabled":true,"priority":2,"condition":{"threatLevel":"HIGH","scoreGapGte":15,"consecutiveRoundsGte":2},"actions":["DECEPTION_CAPTURE"]}}},"D":{"mode":"RESERVED","actions":{"LOW":["NO_ACTION"],"MEDIUM":["NO_ACTION"],"HIGH":["NO_ACTION"]},"rules":{}}}}',
        'Y', 'admin', now(), '自动处置策略 JSON');

insert into config (config_id, config_name, config_key, config_value, config_type, create_by, create_time, remark)
values (115, '自动处置捕获点', 'sys.countermeasure.capture_point', '[114.425033,22.699680,20]', 'Y', 'admin', now(), '自动处置捕获点，经纬高 JSON 数组');

create
    definer = root@`%` procedure clean_old_algorithm_data()
BEGIN
    -- 保留最近7天数据，删除更早的数据
    DELETE FROM algorithm_object_detection WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
    DELETE FROM algorithm_track_prediction WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
    DELETE FROM algorithm_data_fusion WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
    DELETE FROM algorithm_geo_position_validator WHERE created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);
END;

create definer = root@`%` event event_clean_algorithm_data on schedule
    every '1' DAY
        starts '2025-05-15 18:47:07'
    enable
    do
    CALL clean_old_algorithm_data();


