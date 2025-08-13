package com.lth.forum.util;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @program: jnu_forum-master
 * @description:
 * @author: Melo
 * @create: 2025-02-26 16:12
 * @Version 1.0
 **/
public class StockUtil {
    // 每个时间段的权重（基于您提供的实际数据）
    // 时间段：9:30-9:45, 9:45-10:00, 10:00-10:15, 10:15-10:30, 10:30-10:45,
    // 10:45-11:00, 11:00-11:15, 11:15-11:30, 13:00-13:15, 13:15-13:30,
    // 13:30-13:45, 13:45-14:00, 14:00-14:15, 14:15-14:30, 14:30-14:45, 14:45-15:00
    public static double[] weights = {
            0.232, 0.122, 0.089, 0.077, 0.066,
            0.061, 0.046, 0.038, 0.058, 0.050,
            0.043, 0.037, 0.038, 0.039, 0.049, 0.084
    };

    /**
     * 判断是否是受限制的股票（科创板/北交所）
     */
    public static boolean isRestrictedStock(String tsCode) {
        return tsCode.startsWith("688") || tsCode.startsWith("8");
    }

    /**
     * 判断是否适用10%涨跌幅限制（主板股票）
     */
    public static boolean is10cm(String tsCode) {
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
    public static boolean is20cm(String tsCode) {
        // 创业板：300开头 | 科创板：688开头
        return tsCode.startsWith("300") || tsCode.startsWith("68");
    }

    /**
     * 新增30cm判断（北交所专用）
     */
    public static boolean is30cm(String tsCode) {
        // 北交所：8开头
        return tsCode.startsWith("8");
    }

    /**
     * 根据当前时间和当前成交量估算全天成交量
     *
     * @param currentTime   当前时间
     * @param currentVolume 当前成交量
     * @return 估算的全天成交量
     */
    public static long estimateDailyVolume(LocalDateTime currentTime, long currentVolume) {
        // 获取当前时间部分
        LocalTime time = currentTime.toLocalTime();

        // 如果不在交易时间内
        if (time.isBefore(LocalTime.of(9, 30)) || time.isAfter(LocalTime.of(15, 0))) {
            return 0L;
        }
        if (time.isAfter(LocalTime.of(15, 0))) {
            return currentVolume;
        }

        // 如果是午休时间，使用上午结束时的权重(65.5%)
        if (time.isAfter(LocalTime.of(11, 30)) && time.isBefore(LocalTime.of(13, 0))) {
            double accumulatedWeight = 0;
            // 遍历weights 得到上午累计权重
            for (int i = 0; i < 8; i++) { // 上午8个时间段
                accumulatedWeight += weights[i];
            }
            return (long) (currentVolume / accumulatedWeight);
        }

        // 计算累积权重
        double accumulatedWeight = calculateAccumulatedWeight(time);

        // 避免除零错误
        if (accumulatedWeight <= 0) {
            throw new IllegalArgumentException("无法计算权重");
        }

        // 估算全天成交量
        return (long) (currentVolume / accumulatedWeight);
    }

    /**
     * 计算到指定时间的累积权重
     *
     * @param time 当前时间
     * @return 累积权重（0-1之间的小数）
     */
    private static double calculateAccumulatedWeight(LocalTime time) {
        double accumulated = 0.0;

        // 上午交易时间前的权重累加
        if (time.isAfter(LocalTime.of(9, 30)) && time.isBefore(LocalTime.of(12, 59))) {
            if (time.isBefore(LocalTime.of(9, 45))) {
                // 在9:30-9:45之间
                accumulated += weights[0] * (time.toSecondOfDay() - LocalTime.of(9, 30).toSecondOfDay()) / 900.0;
            } else {
                accumulated += weights[0];

                if (time.isAfter(LocalTime.of(10, 0))) {
                    accumulated += weights[1];
                    if (time.isAfter(LocalTime.of(10, 15))) {
                        accumulated += weights[2];
                        if (time.isAfter(LocalTime.of(10, 30))) {
                            accumulated += weights[3];
                            if (time.isAfter(LocalTime.of(10, 45))) {
                                accumulated += weights[4];
                                if (time.isAfter(LocalTime.of(11, 0))) {
                                    accumulated += weights[5];
                                    if (time.isAfter(LocalTime.of(11, 15))) {
                                        accumulated += weights[6];
                                        if (time.isAfter(LocalTime.of(11, 30))) {
                                            accumulated += weights[7];
                                        } else {
                                            // 在11:15-11:30之间
                                            accumulated += weights[7] * (time.toSecondOfDay() - LocalTime.of(11, 15).toSecondOfDay()) / 900.0;
                                        }
                                    } else {
                                        // 在11:00-11:15之间
                                        accumulated += weights[6] * (time.toSecondOfDay() - LocalTime.of(11, 0).toSecondOfDay()) / 900.0;
                                    }
                                } else {
                                    // 在10:45-11:00之间
                                    accumulated += weights[5] * (time.toSecondOfDay() - LocalTime.of(10, 45).toSecondOfDay()) / 900.0;
                                }
                            } else {
                                // 在10:30-10:45之间
                                accumulated += weights[4] * (time.toSecondOfDay() - LocalTime.of(10, 30).toSecondOfDay()) / 900.0;
                            }
                        } else {
                            // 在10:15-10:30之间
                            accumulated += weights[3] * (time.toSecondOfDay() - LocalTime.of(10, 15).toSecondOfDay()) / 900.0;
                        }
                    } else {
                        // 在10:00-10:15之间
                        accumulated += weights[2] * (time.toSecondOfDay() - LocalTime.of(10, 0).toSecondOfDay()) / 900.0;
                    }
                } else {
                    // 在9:45-10:00之间
                    accumulated += weights[1] * (time.toSecondOfDay() - LocalTime.of(9, 45).toSecondOfDay()) / 900.0;
                }
            }
        } else {
            //累加早上的
            for (int i = 0; i < 8; i++) {
                accumulated += weights[i];
            }
            // 再加上下午已过的完整时间段权重
            if (time.isAfter(LocalTime.of(13, 15))) {
                accumulated += weights[8];
                if (time.isAfter(LocalTime.of(13, 30))) {
                    accumulated += weights[9];
                    if (time.isAfter(LocalTime.of(13, 45))) {
                        accumulated += weights[10];
                        if (time.isAfter(LocalTime.of(14, 0))) {
                            accumulated += weights[11];
                            if (time.isAfter(LocalTime.of(14, 15))) {
                                accumulated += weights[12];
                                if (time.isAfter(LocalTime.of(14, 30))) {
                                    accumulated += weights[13];
                                    if (time.isAfter(LocalTime.of(14, 45))) {
                                        accumulated += weights[14];
                                        if (time.isAfter(LocalTime.of(15, 0))) {
                                            accumulated += weights[15];
                                        } else {
                                            // 在14:45-15:00之间
                                            accumulated += weights[15] * (time.toSecondOfDay() - LocalTime.of(14, 45).toSecondOfDay()) / 900.0;
                                        }
                                    } else {
                                        // 在14:30-14:45之间
                                        accumulated += weights[14] * (time.toSecondOfDay() - LocalTime.of(14, 30).toSecondOfDay()) / 900.0;
                                    }
                                } else {
                                    // 在14:15-14:30之间
                                    accumulated += weights[13] * (time.toSecondOfDay() - LocalTime.of(14, 15).toSecondOfDay()) / 900.0;
                                }
                            } else {
                                // 在14:00-14:15之间
                                accumulated += weights[12] * (time.toSecondOfDay() - LocalTime.of(14, 0).toSecondOfDay()) / 900.0;
                            }
                        } else {
                            // 在13:45-14:00之间
                            accumulated += weights[11] * (time.toSecondOfDay() - LocalTime.of(13, 45).toSecondOfDay()) / 900.0;
                        }
                    } else {
                        // 在13:30-13:45之间
                        accumulated += weights[10] * (time.toSecondOfDay() - LocalTime.of(13, 30).toSecondOfDay()) / 900.0;
                    }
                } else {
                    // 在13:15-13:30之间
                    accumulated += weights[9] * (time.toSecondOfDay() - LocalTime.of(13, 15).toSecondOfDay()) / 900.0;
                }
            } else {
                // 在13:00-13:15之间
                accumulated += weights[8] * (time.toSecondOfDay() - LocalTime.of(13, 0).toSecondOfDay()) / 900.0;
            }
        }

        return accumulated;
    }
}
