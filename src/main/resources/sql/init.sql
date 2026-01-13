CREATE TABLE IF NOT EXISTS `account` (
  `uid` BIGINT NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `nickname` VARCHAR(64) DEFAULT NULL,
  `telegram` VARCHAR(64) DEFAULT NULL,
  `remark` VARCHAR(255) DEFAULT NULL,
  `api_token` VARCHAR(64) DEFAULT NULL,
  `balance` DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  `invite_code` VARCHAR(32) DEFAULT NULL,
  `invited_by` BIGINT NOT NULL DEFAULT 0,
  `parent_uid` BIGINT NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uk_account_email` (`email`),
  KEY `idx_account_invite_code` (`invite_code`),
  KEY `idx_account_parent_uid` (`parent_uid`),
  KEY `idx_account_invited_by` (`invited_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

