package com.xzp.forum.util;

/**
 * @program: jnu_forum-master
 * @description:
 * @author: Melo
 * @create: 2025-02-26 16:12
 * @Version 1.0
 **/
public class StockUtil {
    /**
     * 判断是否是受限制的股票（科创板/北交所）
     */
    public static boolean isRestrictedStock(String tsCode) {
        return tsCode.startsWith("688") || tsCode.startsWith("8");
    }

    /**
     * 判断是否适用10%涨跌幅限制（主板股票）
     */
    public static boolean is10cm(String tsCode){
        // 沪市主板：600/601/603/605 开头（排除688科创板）
        boolean shMainBoard = tsCode.startsWith("6") && !tsCode.startsWith("68");
        // 深市主板：000/001/002/003 开头
        boolean szMainBoard = tsCode.startsWith("000")
                || tsCode.startsWith("001")
                || tsCode.startsWith("002")
                || tsCode.startsWith("003");
        return shMainBoard || szMainBoard;
    }

    /**
     * 判断是否适用20%涨跌幅限制（创业板/科创板）
     */
    public static boolean is20cm(String tsCode){
        // 创业板：300开头 | 科创板：688开头
        return tsCode.startsWith("300") || tsCode.startsWith("68");
    }

    /**
     * 新增30cm判断（北交所专用）
     */
    public static boolean is30cm(String tsCode){
        // 北交所：8开头
        return tsCode.startsWith("8");
    }

}
