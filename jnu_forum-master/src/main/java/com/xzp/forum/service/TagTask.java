package com.xzp.forum.service;

import com.google.common.collect.Lists;
import com.xzp.forum.enums.LeafTag;
import com.xzp.forum.mapper.StockBasicMapper;
import com.xzp.forum.mapper.StockDailyMapper;
import com.xzp.forum.mapper.UserTagRelationMapper;
import com.xzp.forum.model.StockBasic;
import com.xzp.forum.model.StockDaily;
import com.xzp.forum.model.UserTagDTO;
import com.xzp.forum.model.UserTagRelation;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @program: yqtrack.java.ims.backend
 * @description: 用户标签打标定时服务
 * @author: Melo
 * @create: 2024-11-06 10:26
 * @Version 1.0
 **/
@Component("TagTask")
public class TagTask {
    private Map<String, Long> tagMap = new HashMap();
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private int corePoolSize = 8;

    private static final ThreadLocal<Integer> DB_NO = new ThreadLocal<>();
    /**
     * 最大可创建的线程数
     */
    private int maxPoolSize = 8;

    @Resource
    private StockBasicMapper stockBasicMapper;
    @Resource
    private UserTagRelationMapper userTagRelationMapper;
    @Resource
    private StockDailyMapper stockDailyMapper;
    /**
     * 线程池维护线程所允许的空闲时间
     */
    private int keepAliveSeconds = 60;

    /**
     * 队列长度
     */
    private static final Integer PAGE_SIZE = 200;

    @PostConstruct
    public void init() {
        //线程池初始化
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(PAGE_SIZE);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        threadPoolTaskExecutor = executor;
        tagMap.put(LeafTag._10CM.getCode(), LeafTag._10CM.getId());
        tagMap.put(LeafTag._20CM.getCode(), LeafTag._20CM.getId());
        tagMap.put(LeafTag.CAN_DEAL.getCode(), LeafTag.CAN_DEAL.getId());
        tagMap.put(LeafTag.STABLE.getCode(), LeafTag.STABLE.getId());
        tagMap.put(LeafTag.UP.getCode(), LeafTag.UP.getId());
        tagMap.put(LeafTag.DOWN.getCode(), LeafTag.DOWN.getId());
        tagMap.put(LeafTag.FLAT.getCode(), LeafTag.FLAT.getId());
    }

    /**
     * 用户打标
     */
    public void setUserTag(String symbol, LocalDate date) {
        try {
            System.out.println("TagTaskstart");
            dealTagByAll(symbol, date);
        } finally {
            System.out.println("TagTaskend");
        }
    }


    /**
     * 所有用户口径
     */
    private void dealTagByAll(String symbol, LocalDate date) {
        List<UserTagDTO> allNowList = Collections.synchronizedList(new ArrayList<>());
        Integer total = stockBasicMapper.count(symbol);
        //防止深分页
        String lastSymbol = null;
        //循环次数
        Integer loopNum = total / PAGE_SIZE + 1;
        for (int i = 1; i <= loopNum; i++) {
            List<StockBasic> stockList = stockBasicMapper.getByPageSize(lastSymbol, PAGE_SIZE, symbol);
            if (CollectionUtils.isEmpty(stockList)) {
                continue;
            }
            CountDownLatch countDownLatch = new CountDownLatch(stockList.size());
            lastSymbol = stockList.get(stockList.size() - 1).getSymbol();
            for (StockBasic user : stockList) {
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //跑出当前标签
                            List<UserTagDTO> nowList = runAllStockTaskCore(user, date);
                            //非遍历用户即可得到的判断
                            allNowList.addAll(nowList);
                        } catch (Exception ex) {
                            System.out.println("error"+user.getSymbol());
                            ex.printStackTrace();
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        dealData(allNowList, symbol, date);
    }


    /**
     * 数据库操作
     *
     * @param nowList
     * @param symbol
     */
    private void dealData(List<UserTagDTO> nowList, String symbol, LocalDate date) {
        nowList.stream().forEach(x -> x.setDate(date));
        List<LeafTag> tags = new ArrayList(List.of(LeafTag.values()));
        List<String> tagKeys = tags.stream().map(LeafTag::getCode).collect(Collectors.toList());
        List<Long> tagIds = tagMap.entrySet().stream().filter(x -> tagKeys.contains(x.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
        List<UserTagDTO> oldList = userTagRelationMapper.queryByTagIdsAndSymbol(tagIds, symbol);
        //比对出新增与删除
        List<UserTagDTO> addList = nowList.stream().filter(x -> !oldList.contains(x)).
                distinct().collect(Collectors.toList());
        List<UserTagDTO> deleteList = oldList.stream().filter(x -> !nowList.contains(x)).collect(Collectors.toList());
        //db批量操作
        if (!CollectionUtils.isEmpty(deleteList)) {
            List<List<UserTagDTO>> partitionList = Lists.partition(deleteList, 500);
            for (List<UserTagDTO> list : partitionList) {
                userTagRelationMapper.batchDelete(list.stream().map(UserTagDTO::getId).collect(Collectors.toList()));
            }
        }
        if (!CollectionUtils.isEmpty(addList)) {
            List<List<UserTagDTO>> partitionList = Lists.partition(addList, 500);
            for (List<UserTagDTO> list : partitionList) {
                userTagRelationMapper.batchInsert(list.stream().map(x -> {
                    return new UserTagRelation(null, x.getSymbol(), x.getFtagId(), null, date, LeafTag.fromId(x.getFtagId()).getDescription());
                }).collect(Collectors.toList()));
            }
        }

    }

    /**
     * 处理所有用户
     *
     * @param stock
     * @return
     */
    private List<UserTagDTO> runAllStockTaskCore(StockBasic stock, LocalDate date) {
        List<UserTagDTO> resultList = new ArrayList<>();

        // 打_10CM标签逻辑
      /*  if (StockUtil.is10cm(stock.getTsCode())) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag._10CM.getCode()));
        }

        // 打_20CM标签逻辑
        if (StockUtil.is20cm(stock.getTsCode())) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag._20CM.getCode()));
        }

        // 打CAN_DEAL标签逻辑
        if (!StockUtil.isRestrictedStock(stock.getTsCode())) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.CAN_DEAL.getCode()));
        }*/

     /*   String code = stock.getTsCode();
        List<StockDaily> stockDailyList = stockDailyMapper.selectByTsCode(code);
        //缩量至平稳
        if (stable(stockDailyList)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.STABLE.getCode()));
        }*/

        //阳线
        StockDaily stockDaily = stockDailyMapper.selectByTsCodeAndDate(stock.getTsCode(), date);
        if (stockDaily != null && stockDaily.getChange().compareTo(BigDecimal.ZERO) > 0) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.UP.getCode(), date));
        } else if (stockDaily != null && stockDaily.getChange().compareTo(BigDecimal.ZERO) < 0) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.DOWN.getCode(), date));
        } else {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.FLAT.getCode(), date));
        }

        return resultList;
    }

    /**
     * 是否缩量至平稳
     *
     * @param sortedList
     * @return
     */
    private boolean stable(List<StockDaily> sortedList) {
        if (sortedList == null || sortedList.size() < 10) return false; // 扩大观察窗口

        // 参数配置
        BigDecimal VOLUME_DROP_RATIO = new BigDecimal("0.5"); // 缩量比例阈值（50%）
        BigDecimal RECENT_CHG_LIMIT = new BigDecimal("2");    // 最近一日涨跌幅限制
        BigDecimal STABLE_DAYS = new BigDecimal(3);           // 盘整持续天数要求

        try {
            // 1. 寻找放量峰值（取历史最大成交量）
            BigDecimal maxVol = sortedList.stream()
                    .map(StockDaily::getVol)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            // 2. 定位缩量起始点（成交量下降超过50%且持续）
            int stableStartIndex = -1;
            for (int i = 1; i < sortedList.size(); i++) {
                BigDecimal currentVol = sortedList.get(i).getVol();
                if (currentVol.compareTo(maxVol.multiply(VOLUME_DROP_RATIO)) < 0) {
                    stableStartIndex = i;
                    break;
                }
            }
            // 在定位缩量起始点后添加（检查是否持续缩量）
            for (int i = stableStartIndex; i < sortedList.size(); i++) {
                BigDecimal vol = sortedList.get(i).getVol();
                if (vol.compareTo(maxVol.multiply(VOLUME_DROP_RATIO)) > 0) {
                    return false; // 缩量后再次放量则不符合
                }
            }

            // 未找到有效缩量点
            if (stableStartIndex == -1 || (sortedList.size() - stableStartIndex) < STABLE_DAYS.intValue()) {
                return false;
            }

            // 3. 检查盘整阶段特征（最后5日）
            List<StockDaily> stablePhase = sortedList.subList(
                    Math.max(stableStartIndex, sortedList.size() - STABLE_DAYS.intValue()),
                    sortedList.size()
            );

            // 3.1 量能稳定性检查（成交量波动率<20%）
            BigDecimal avgVol = stablePhase.stream()
                    .map(StockDaily::getVol)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(STABLE_DAYS, 4, RoundingMode.HALF_UP);

            BigDecimal volVariance = stablePhase.stream()
                    .map(d -> d.getVol().subtract(avgVol).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal volStd = new BigDecimal(Math.sqrt(volVariance.divide(STABLE_DAYS, 8, RoundingMode.HALF_UP).doubleValue()));

            // 4. 价格稳定性检查（收盘价标准差<1%）
            BigDecimal avgClose = stablePhase.stream()
                    .map(StockDaily::getClose)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(STABLE_DAYS, 4, RoundingMode.HALF_UP);

            BigDecimal priceVariance = stablePhase.stream()
                    .map(d -> d.getClose().subtract(avgClose).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal priceStd = new BigDecimal(Math.sqrt(priceVariance.divide(STABLE_DAYS, 8, RoundingMode.HALF_UP).doubleValue()));

            // 5. 最终交易日限制（最后一日涨跌幅）
            StockDaily lastDay = sortedList.get(sortedList.size() - 1);
            boolean lastDayValid = lastDay.getPctChg().abs().compareTo(RECENT_CHG_LIMIT) <= 0;

            return volStd.compareTo(avgVol.multiply(new BigDecimal("0.2"))) < 0 && // 成交量波动<20%
                    priceStd.compareTo(avgClose.multiply(new BigDecimal("0.015"))) < 0 && // 价格波动<1%
                    lastDayValid;
        } catch (Exception e) {
            return false;
        }
    }


    private UserTagDTO buildTagRelation(String symbol, String code, LocalDate date) {
        return new UserTagDTO(1L, symbol, tagMap.get(code), date);
    }

}