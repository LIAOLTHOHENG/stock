CREATE DATABASE `jnu_forum` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

  -- jnu_forum.stock_basic definition

CREATE TABLE `stock_basic` (
                               `ts_code` varchar(20) NOT NULL COMMENT 'TS代码',
                               `symbol` varchar(10) NOT NULL COMMENT '股票代码',
                               `name` varchar(50) NOT NULL COMMENT '股票名称',
                               `industry` varchar(50) DEFAULT NULL COMMENT '所属行业',
                               `area` varchar(50) DEFAULT NULL COMMENT '地域',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`ts_code`),
                               UNIQUE KEY `idx_symbol` (`symbol`),
                               KEY `idx_industry` (`industry`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股票基本信息表';


-- jnu_forum.stock_daily definition

CREATE TABLE `stock_daily` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                               `ts_code` varchar(20) NOT NULL COMMENT 'TS股票代码',
                               `trade_date` date NOT NULL COMMENT '交易日期',
                               `open` decimal(10,2) DEFAULT NULL COMMENT '开盘价',
                               `high` decimal(10,2) DEFAULT NULL COMMENT '最高价',
                               `low` decimal(10,2) DEFAULT NULL COMMENT '最低价',
                               `close` decimal(10,2) DEFAULT NULL COMMENT '收盘价',
                               `pre_close` decimal(10,2) DEFAULT NULL COMMENT '前收盘价',
                               `pre_open` decimal(10,2) DEFAULT NULL COMMENT '前开盘价',
                               `pre_low` decimal(10,2) DEFAULT NULL COMMENT '前最低价',
                               `pre_high` decimal(10,2) DEFAULT NULL COMMENT '前收盘价',
                               `change` decimal(10,2) DEFAULT NULL COMMENT '涨跌额',
                               `pct_chg` decimal(10,3) DEFAULT NULL COMMENT '涨跌幅(%)',
                               `vol` decimal(15,2) DEFAULT NULL COMMENT '成交量(手)',
                               `amount` decimal(15,2) DEFAULT NULL COMMENT '成交额(千元)',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `uniq_trade` (`ts_code`,`trade_date`),
                               KEY `idx_trade_date` (`trade_date`)
) ENGINE=InnoDB AUTO_INCREMENT=64301 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股票日线数据表';


-- jnu_forum.user_tag_relation definition

CREATE TABLE `user_tag_relation` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                     `symbol` bigint NOT NULL COMMENT '代码',
                                     `FTagId` bigint NOT NULL COMMENT '标签ID',
                                     `FCreateTime` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                     `date` date NOT NULL COMMENT '交易日期',
                                     `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
                                     PRIMARY KEY (`id`) USING BTREE,
                                     UNIQUE KEY `user_tag_relation_symbol_IDX` (`symbol`,`FTagId`,`date`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=29780 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签关系表';