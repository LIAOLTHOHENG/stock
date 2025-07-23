package com.xzp.forum.util;

import com.xzp.forum.model.StockDaily;

import java.math.BigDecimal;

public class StockLimitUtils {
    /**
     * 判断是否涨停（动态涨跌幅限制）
     */
    public static boolean isLimitUp(StockDaily stock) {
        BigDecimal limit = getLimitRate(stock.getTsCode());
        return stock.getPctChg().compareTo(limit) >= 0;
    }

    /**
     * 判断是否跌停（动态涨跌幅限制）
     */
    public static boolean isLimitDown(StockDaily stock) {
        BigDecimal limit = getLimitRate(stock.getTsCode());
        return stock.getPctChg().compareTo(limit.negate()) <= 0;
    }

    /**
     * 是否成功封板（涨停/跌停）
     * @param stock
     * @return true 表示成功封板
     */
    public static boolean isSuccessLimitUp(StockDaily stock) {
        if (stock == null) return false;

        // 1. 先判断是否涨停或跌停
        boolean isLimit = isLimitUp(stock) || isLimitDown(stock);
        if (!isLimit) return false;

        // 2. 涨停封板判断：收盘价 == 最高价
        if (isLimitUp(stock) && stock.getClose().compareTo(stock.getHigh()) == 0) {
            return true;
        }

        return false;
    }

    //是否触及涨停
    public static boolean touchLimitUp(StockDaily stock){
        if (stock == null) return false;

        // 获取涨停价
        BigDecimal limitPrice = getLimitPrice(stock, true);
        if (limitPrice == null) return false;

        // 判断最高价是否 >= 涨停价（触及）
        boolean touched = stock.getHigh().compareTo(limitPrice) >= 0;
        // 判断收盘价是否 < 涨停价（未封死）
        boolean notSealed = stock.getClose().compareTo(limitPrice) < 0;

        return touched && notSealed;
    }

    /**
     * 获取涨停价或跌停价
     * @param stock
     * @param isUp true=涨停价，false=跌停价
     * @return 计算后的价格
     */
    public static BigDecimal getLimitPrice(StockDaily stock, boolean isUp) {
        if (stock == null || stock.getPreClose() == null) return null;

        BigDecimal preClose = stock.getPreClose();
        BigDecimal limitRate = getLimitRate(stock.getTsCode());

        if (isUp) {
            // 涨停价 = 前收盘价 * (1 + limitRate%)
            return preClose.multiply(BigDecimal.valueOf(1).add(limitRate.divide(BigDecimal.valueOf(100))));
        } else {
            // 跌停价 = 前收盘价 * (1 - limitRate%)
            return preClose.multiply(BigDecimal.valueOf(1).subtract(limitRate.divide(BigDecimal.valueOf(100))));
        }
    }

    /**
     * 根据股票代码获取涨跌幅限制
     */
    private static BigDecimal getLimitRate(String tsCode) {
        if (tsCode == null) {
            return BigDecimal.valueOf(10); // 默认主板
        }
        if (tsCode.contains("ST") || tsCode.contains("*ST")) {
            return BigDecimal.valueOf(5); // ST股
        } else if (tsCode.startsWith("300") || tsCode.startsWith("688")) {
            return BigDecimal.valueOf(20); // 创业板/科创板
        } else {
            return BigDecimal.valueOf(10); // 主板
        }
    }
}
