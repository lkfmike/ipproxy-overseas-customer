CREATE TABLE IF NOT EXISTS `account`
(
    `uid`         BIGINT         NOT NULL AUTO_INCREMENT,
    `email`       VARCHAR(255)   NOT NULL,
    `password`    VARCHAR(100)   NOT NULL,
    `nickname`    VARCHAR(64)             DEFAULT NULL,
    `telegram`    VARCHAR(64)             DEFAULT NULL,
    `remark`      VARCHAR(255)            DEFAULT NULL,
    `api_token`   VARCHAR(64)             DEFAULT NULL,
    `balance`     DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    `invite_code` VARCHAR(32)             DEFAULT NULL,
    `invited_by`  BIGINT         NOT NULL DEFAULT 0,
    `parent_uid`  BIGINT         NOT NULL DEFAULT 0,
    `created_at`  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`uid`),
    UNIQUE KEY `uk_account_email` (`email`),
    KEY `idx_account_invite_code` (`invite_code`),
    KEY `idx_account_parent_uid` (`parent_uid`),
    KEY `idx_account_invited_by` (`invited_by`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `area_region_info`
(
    `id`             int            NOT NULL AUTO_INCREMENT,
    `area`           varchar(100)   NOT NULL DEFAULT '',
    `area_cn`        varchar(100)   NOT NULL DEFAULT '',
    `region`         varchar(100)   NOT NULL DEFAULT '',
    `region_cn`      varchar(100)   NOT NULL DEFAULT '',
    `is_show`        int            NOT NULL DEFAULT '1',
    `is_available`   int            NOT NULL DEFAULT '1',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

CREATE TABLE `static_proxy_price`
(
    `id`             int            NOT NULL AUTO_INCREMENT,
    `uid`            int            NOT NULL DEFAULT '0',
    `type`           varchar(25)    NOT NULL DEFAULT '',
    `quality`        varchar(25)    NOT NULL DEFAULT '',
    `area`           varchar(100)   NOT NULL DEFAULT '',
    `region`         varchar(100)   NOT NULL DEFAULT '',
    `price`          decimal(10, 2) NOT NULL DEFAULT '0.00',
    `discount_price` decimal(10, 2) NOT NULL DEFAULT '0.00',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB;

