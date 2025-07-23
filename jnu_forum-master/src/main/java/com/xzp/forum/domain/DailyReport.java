package com.xzp.forum.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * 日结数据
 * @TableName daily_report
 */
@Data
public class DailyReport {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    /**
     * 中位数涨跌幅(%)
     */
    private BigDecimal midPctChg;

    /**
     * 上涨家数
     */
    private Integer upAmount;

    /**
     * 下跌家数
     */
    private Integer downAmount;

    /**
     * 涨停家数
     */
    private Integer limitUpAmount;

    /**
     * 跌停家数
     */
    private Integer limitDownAmount;

    /**
     * 封板率
     */
    private Integer limitUpPct;

    /**
     * 创建时间
     */
    private Date createTime;

}