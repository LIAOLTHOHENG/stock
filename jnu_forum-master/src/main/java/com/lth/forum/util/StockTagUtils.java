package com.lth.forum.util;

import com.lth.forum.enums.LeafTag;
import com.lth.forum.model.StockDaily;
import com.lth.forum.model.StockBasic;
import com.lth.forum.domain.StockRealtime;
import com.lth.forum.model.UserTagDTO;
import com.lth.forum.model.UserTagRealtimeDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class StockTagUtils {
    
    /**
     * 处理共同的打标逻辑
     * @param stock 股票基本信息
     * @param today 今日数据（盘后为StockDaily，盘中为StockRealtime）
     * @param yesterday 昨日数据
     * @param sortedList 历史数据列表
     * @param tagConsumer 标签处理函数，用于添加标签
     */
    public static void processCommonTags(StockBasic stock, Object today, StockDaily yesterday, 
                                       List<StockDaily> sortedList, BiConsumer<String, String> tagConsumer) {
        // 获取今日振幅
        BigDecimal todayChange;
        BigDecimal todayOpen;
        BigDecimal todayClose;

        if (today instanceof StockDaily) {
            StockDaily todayStock = (StockDaily) today;
            todayChange = todayStock.getClose().subtract(todayStock.getOpen());
            todayOpen = todayStock.getOpen();
            todayClose = todayStock.getClose();
        } else if (today instanceof StockRealtime) {
            StockRealtime todayStock = (StockRealtime) today;
            todayChange = todayStock.getClose().subtract(todayStock.getOpen());
            todayOpen = todayStock.getOpen();
            todayClose = todayStock.getClose();
            long vol = StockUtil.estimateDailyVolume(LocalDateTime.now(), todayStock.getVol().longValue())/100;
            sortedList.add(0, new StockDaily() {{
                setVol(new BigDecimal(vol));
            }});
        } else {
            return;
        }
        
        //昨日振幅
        BigDecimal yesterdayChange = yesterday.getClose().subtract(yesterday.getOpen());
        
        if (yesterdayChange.compareTo(BigDecimal.ZERO) <= 0//昨日阴线
                && todayOpen.compareTo(yesterday.getClose()) > 0 && todayClose.compareTo(yesterday.getClose()) > 0 //今天开盘价，收盘价 均大于昨日收盘价
                && todayOpen.compareTo(yesterday.getOpen()) < 0 && todayClose.compareTo(yesterday.getOpen()) < 0) {//今天开盘价，收盘价 均小于昨日开盘价
            tagConsumer.accept(stock.getSymbol(), LeafTag.UP_YUNXIAN.getCode());
        } else if (yesterdayChange.compareTo(BigDecimal.ZERO) <= 0 
                && todayChange.divide(yesterday.getClose(), 3, RoundingMode.HALF_UP).abs().compareTo(new BigDecimal("0.01")) <= 0
                && todayOpen.compareTo(yesterday.getClose()) <= 0 && todayClose.compareTo(yesterday.getClose()) <= 0) {
            tagConsumer.accept(stock.getSymbol(), LeafTag.UP_SHIZI.getCode());
        }
        
        //成交量相关的
        if (sortedList.size() == 6) {
            boolean fail = false;
            //从前往后 找出最高点 以及对应的下标
            long anchor = 0;
            int index = 0;
            for (int i = sortedList.size() - 1; i >= 0; i--) {
                StockDaily stockDaily = sortedList.get(i);
                //更新锚点
                if (stockDaily.getVol().longValue() > anchor) {
                    anchor = stockDaily.getVol().longValue();
                    index = i;
                }
            }
            //当前量最大
            if (index <= 1) {
                fail = true;
            }
            if (!fail) {
                //平缓向下，如果向上，不可超过锚点的10%
                for (int i = index; i >= 1; i--) {
                    StockDaily thisDay = sortedList.get(i);
                    StockDaily nextDay = sortedList.get(i - 1);
                    if (nextDay == null || thisDay == null) {
                        break;
                    }
                    long todayVol = thisDay.getVol().longValue();
                    long nextDayVol = nextDay.getVol().longValue();
                    if (nextDayVol > todayVol * StockUtil.stable) {
                        fail = true;
                        break;
                    }
                }
            }
            if (!fail) {
                tagConsumer.accept(stock.getSymbol(), LeafTag.STABLE_VOLUME.getCode());
            }
        }

        //今日阳线
        if (todayChange.compareTo(BigDecimal.ZERO) > 0) {
            tagConsumer.accept(stock.getSymbol(), LeafTag.YANGXIAN.getCode());
            //初始化数据兼容处理
            if (yesterday != null) {
                if (yesterday.getOpen().compareTo(yesterday.getClose()) > 0 //昨天阴线 //今日阳线
                        && todayClose.compareTo(yesterday.getClose()) <= 0) {//今天收盘价小于等于昨天的收盘价
                    tagConsumer.accept(stock.getSymbol(), LeafTag.YANGXIAN_GUXING.getCode());
                } else if (yesterday.getOpen().compareTo(yesterday.getClose()) > 0//昨天阴线 //今日阳线
                        && todayClose.compareTo(yesterday.getClose()) >= 0 //今日收盘价大于等于昨日收盘价
                        && todayClose.compareTo(yesterday.getOpen()) <= 0 //今日收盘价小于等于昨日开盘价
                        && todayOpen.compareTo(yesterday.getClose()) <= 0) {//今日开盘价小于昨日收盘价
                    tagConsumer.accept(stock.getSymbol(), LeafTag.UP_INSERTION.getCode());
                } else if (yesterday.getOpen().compareTo(yesterday.getClose()) > 0 //昨天阴线 //今日阳线
                        && todayOpen.compareTo(yesterday.getClose()) <= 0//今日开盘价小于昨日收盘价
                        && todayClose.compareTo(yesterday.getOpen()) >= 0) {//今日收盘价大于等于昨日开盘价
                    tagConsumer.accept(stock.getSymbol(), LeafTag.UP_HUG.getCode());
                }
            }
        } else if (todayChange.compareTo(BigDecimal.ZERO) < 0) {
            tagConsumer.accept(stock.getSymbol(), LeafTag.YINXIAN.getCode());
            //初始化数据兼容处理
            if (yesterday != null) {
                if (yesterday.getClose().compareTo(yesterday.getOpen()) > 0 && todayClose.compareTo(yesterday.getClose()) >= 0) {
                    tagConsumer.accept(stock.getSymbol(), LeafTag.YINXIAN_GUXING.getCode());
                }
            }
        }
    }
    
    /**
     * 处理涨幅标签
     */
    public static void processChangeTags(StockDaily today, BiConsumer<String, String> tagConsumer, String symbol) {
        //涨幅
        if (today.getChange().compareTo(BigDecimal.ZERO) > 0) {
            tagConsumer.accept(symbol, LeafTag.UP.getCode());
        } else if (today.getChange().compareTo(BigDecimal.ZERO) < 0) {
            tagConsumer.accept(symbol, LeafTag.DOWN.getCode());
        } else {
            tagConsumer.accept(symbol, LeafTag.FLAT.getCode());
        }
    }

    /**
     * 处理用户标签列表，如果同时存在FtagId为1和2的元素，则删除FtagId为2的元素
     *
     * @param list 用户标签列表，支持UserTagDTO或UserTagRealtimeDTO类型
     * @param <T>  泛型参数，必须包含getFtagId()方法
     */
    public static <T> void processUserTagList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 检查是否存在FtagId为1的元素
        boolean hasFtagId1 = list.stream().anyMatch(item -> getFtagId(item) == LeafTag.YANGXIAN_GUXING.getId());
        // 检查是否存在FtagId为2的元素
        boolean hasFtagId2 = list.stream().anyMatch(item -> getFtagId(item) == LeafTag.UP_SHIZI.getId());

        // 如果同时存在FtagId为1和2的元素，则删除FtagId为2的元素
        if (hasFtagId1 && hasFtagId2) {
            list.removeIf(item -> getFtagId(item) == LeafTag.UP_SHIZI.getId());
        }
    }

    /**
     * 获取对象的FtagId值
     *
     * @param item 对象实例
     * @param <T>  对象类型
     * @return FtagId值
     */
    private static <T> Long getFtagId(T item) {
        if (item instanceof UserTagDTO) {
            return ((UserTagDTO) item).getFtagId();
        } else if (item instanceof UserTagRealtimeDTO) {
            return ((UserTagRealtimeDTO) item).getFtagId();
        } else {
            throw new IllegalArgumentException("不支持的类型: " + item.getClass().getSimpleName());
        }
    }


}