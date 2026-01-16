CREATE TABLE IF NOT EXISTS `account`
(
    `uid`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `email`       VARCHAR(255)   NOT NULL COMMENT '邮箱',
    `password`    VARCHAR(100)   NOT NULL COMMENT '密码',
    `nickname`    VARCHAR(64)             DEFAULT NULL COMMENT '昵称',
    `telegram`    VARCHAR(64)             DEFAULT NULL COMMENT 'Telegram',
    `remark`      VARCHAR(255)            DEFAULT NULL COMMENT '备注',
    `api_token`   VARCHAR(64)             DEFAULT NULL COMMENT 'API令牌',
    `balance`     DECIMAL(18, 2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    `invite_code` VARCHAR(32)             DEFAULT NULL COMMENT '邀请码',
    `invited_by`  BIGINT         NOT NULL DEFAULT 0 COMMENT '邀请人ID',
    `parent_uid`  BIGINT         NOT NULL DEFAULT 0 COMMENT '父级用户ID',
    `created_at`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`uid`),
    UNIQUE KEY `uk_account_email` (`email`),
    KEY `idx_account_invite_code` (`invite_code`),
    KEY `idx_account_parent_uid` (`parent_uid`),
    KEY `idx_account_invited_by` (`invited_by`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3 COMMENT='用户账户表';

CREATE TABLE `area_region_info`
(
    `id`             int            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `area`           varchar(100)   NOT NULL DEFAULT '' COMMENT '区域代码',
    `area_cn`        varchar(100)   NOT NULL DEFAULT '' COMMENT '区域中文名称',
    `region`         varchar(100)   NOT NULL DEFAULT '' COMMENT '地区代码',
    `region_cn`      varchar(100)   NOT NULL DEFAULT '' COMMENT '地区中文名称',
    `is_show`        int            NOT NULL DEFAULT '1' COMMENT '是否显示',
    `is_available`   int            NOT NULL DEFAULT '1' COMMENT '是否可用',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3 COMMENT='地区区域信息表';

CREATE TABLE `static_proxy_price`
(
    `id`             int            NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `uid`            int            NOT NULL DEFAULT '0' COMMENT '用户ID',
    `type`           varchar(25)    NOT NULL DEFAULT '' COMMENT '类型',
    `quality`        varchar(25)    NOT NULL DEFAULT '' COMMENT '质量',
    `area`           varchar(100)   NOT NULL DEFAULT '' COMMENT '区域',
    `region`         varchar(100)   NOT NULL DEFAULT '' COMMENT '地区',
    `price`          decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '价格',
    `discount_price` decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '折扣价格',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3 COMMENT='静态代理价格表';

CREATE TABLE `asyn_order`
(
    `id`                  bigint       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`            varchar(100) NOT NULL DEFAULT '' COMMENT '供应商返回的订单号',
    `system_order_no`     varchar(100) NOT NULL DEFAULT '' COMMENT '系统生成的订单号',
    `uid`                 bigint       NOT NULL COMMENT '用户ID',
    `unit_price`          decimal(10, 2) NOT NULL DEFAULT '0.00' COMMENT '单价',
    `quantity`            int          NOT NULL COMMENT '数量',
    `auto_renew`          tinyint(1)   NOT NULL DEFAULT 0 COMMENT '是否自动续费',
    `protocol`            varchar(100) NOT NULL DEFAULT '' COMMENT '协议',
    `asn_type`            varchar(50)  NOT NULL DEFAULT '' COMMENT 'ASN类型',
    `quality`             varchar(50)  NOT NULL DEFAULT '' COMMENT '质量类型',
    `stock_id`            int          NOT NULL COMMENT '库存ID',
    `dedicated_line`      tinyint(1)   NULL COMMENT '是否选择专线中转',
    `bandwidth`           varchar(10)  NULL COMMENT '专线带宽',
    `created_at`          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `status`              varchar(20)  NOT NULL DEFAULT 'pending' COMMENT '订单状态',
    PRIMARY KEY (`id`),
    KEY `idx_uid` (`uid`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_system_order_no` (`system_order_no`),
    KEY `idx_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3 COMMENT='异步订单表';

