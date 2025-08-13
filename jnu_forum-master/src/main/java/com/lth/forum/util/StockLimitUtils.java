package com.lth.forum.util;

import com.lth.forum.model.StockDaily;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StockLimitUtils {
    // 定义精度容忍度
    private static final BigDecimal TOLERANCE = new BigDecimal("0.001");

    /**
     * 判断是否涨停（动态涨跌幅限制）
     */
    public static boolean isLimitUp(StockDaily stock) {
        // 计算理论涨停价
        BigDecimal limitPrice = getLimitPrice(stock, true);
        // 判断收盘价是否达到或超过理论涨停价（考虑精度误差）
        return stock.getClose().compareTo(limitPrice.subtract(TOLERANCE)) >= 0;
    }

    /**
     * 判断是否跌停（动态涨跌幅限制）
     */
    public static boolean isLimitDown(StockDaily stock) {
        BigDecimal limitPrice = getLimitPrice(stock, false);
        return stock.getClose().compareTo(limitPrice.add(TOLERANCE)) <= 0;
    }

    //是否触及涨停
    public static boolean touchLimitUp(StockDaily stock) {
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
     *
     * @param stock
     * @param isUp  true=涨停价，false=跌停价
     * @return 计算后的价格
     */
    public static BigDecimal getLimitPrice(StockDaily stock, boolean isUp) {
        if (stock == null || stock.getPreClose() == null) return null;

        BigDecimal preClose = stock.getPreClose();
        BigDecimal limitRate = getLimitRate(stock.getTsCode());

        if (isUp) {
            // 涨停价 = 前收盘价 * (1 + limitRate%)
            return preClose.multiply(BigDecimal.ONE.add(limitRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            // 跌停价 = 前收盘价 * (1 - limitRate%)
            return preClose.multiply(BigDecimal.ONE.subtract(limitRate.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * 根据股票代码获取涨跌幅限制
     */
    private static BigDecimal getLimitRate(String tsCode) {
        if (tsCode == null) {
            return BigDecimal.valueOf(10); // 默认主板
        }
        if (tsCode.contains("ST") || tsCode.contains("*ST") || tsCode.contains("**ST")) {
            return BigDecimal.valueOf(5); // ST股
        } else if (tsCode.startsWith("30") || tsCode.startsWith("68")) {
            return BigDecimal.valueOf(20); // 创业板/科创板
        } else {
            return BigDecimal.valueOf(10); // 主板
        }
    }
}
