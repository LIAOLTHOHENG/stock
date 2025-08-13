package com.lth.forum.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 股票日线实时数据表
 * @TableName stock_realtime
 */
@Data
public class StockRealtime {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * TS股票代码
     */
    private String tsCode;

    /**
     * 名称
     */
    private String name;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 前收盘价
     */
    private BigDecimal preClose;

    /**
     * 前开盘价
     */
    private BigDecimal preOpen;

    /**
     * 前最低价
     */
    private BigDecimal preLow;

    /**
     * 前收盘价
     */
    private BigDecimal preHigh;

    /**
     * 涨跌额
     */
    private BigDecimal change;

    /**
     * 涨跌幅(%)
     */
    private BigDecimal pctChg;

    /**
     * 成交量(手)
     */
    private BigDecimal vol;

    /**
     * 成交额(千元)
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private Date createTime;

}