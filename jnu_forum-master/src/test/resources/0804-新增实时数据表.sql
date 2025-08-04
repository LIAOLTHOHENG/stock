CREATE TABLE `stock_realtime` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                  `ts_code` varchar(20) NOT NULL COMMENT 'TS股票代码',
                                  `name` varchar(20) NOT NULL COMMENT '名称',
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
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='股票日线实时数据表';

CREATE TABLE `user_tag_relation_realtime` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
                                              `symbol` varchar(20) NOT NULL COMMENT '代码',
                                              `FTagId` bigint NOT NULL COMMENT '标签ID',
                                              `FCreateTime` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
                                              `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                                              PRIMARY KEY (`id`) USING BTREE,
                                              KEY `symbol` (`symbol`,`FTagId`) USING BTREE,
                                              KEY `tag` (`FTagId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=911798 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签关系表(实时)';