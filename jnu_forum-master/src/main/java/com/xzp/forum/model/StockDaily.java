package com.xzp.forum.model;

/**
 * @program: jnu_forum-master
 * @description:
 * @author: Melo
 * @create: 2025-02-25 14:28
 * @Version 1.0
 **/

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockDaily {
    private Long id;
    private String tsCode;        // TS股票代码
    private LocalDate tradeDate;       // 交易日期
    private BigDecimal open;      // 开盘价
    private BigDecimal high;      // 最高价
    private BigDecimal low;       // 最低价
    private BigDecimal close;     // 收盘价
    private BigDecimal preClose;  // 前收盘价（新增）
    private BigDecimal preOpen = null;   // 前开盘价（新增）
    private BigDecimal preLow = null;    // 前最低价（新增）
    private BigDecimal preHigh = null;   // 前最高价（新增）
    private BigDecimal change;    // 涨跌额（新增）
    private BigDecimal pctChg;    // 涨跌幅（新增）
    private BigDecimal vol;       // 成交量（注意类型改为BigDecimal）
    private BigDecimal amount;    // 成交额（新增）
    private LocalDate createTime;      // 创建时间（新增）
}
