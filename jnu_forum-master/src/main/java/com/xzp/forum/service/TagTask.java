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
import com.xzp.forum.util.StockLimitUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        for (LeafTag tag : LeafTag.values()) {
            tagMap.put(tag.getCode(), tag.getId());
        }
    }

    /**
     * 用户打标
     */
    public void setUserTag(String symbol, LocalDate date, List<LeafTag> tags) {
        try {
            System.out.println("TagTaskstart");
            if (date == null) {
                LocalDate maxDate = userTagRelationMapper.getMaxDate();
                LocalDate now = LocalDate.now();
                //从maxdate到now的每一天都补全
                for (LocalDate i = maxDate.plusDays(1); i.isBefore(now.plusDays(1)); i = i.plusDays(1)) {
                    //剔除工作日 法定节假日
                    if (i.getDayOfWeek().getValue() <= 5) {
                        setUserTag(symbol, i, tags);
                    }
                }
                return;
            }
            dealTagByAll(symbol, date, tags);

        } finally {
            System.out.println("TagTaskend");
        }
    }


    /**
     * 所有用户口径
     */
    private void dealTagByAll(String symbol, LocalDate date, List<LeafTag> tags) {
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
                List<UserTagDTO> finalAllNowList = allNowList;
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //跑出当前标签
                            List<UserTagDTO> nowList = runAllStockTaskCore(user, date);
                            //非遍历用户即可得到的判断
                            finalAllNowList.addAll(nowList);
                        } catch (Exception ex) {
                            System.out.println("error" + user.getSymbol());
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

        if (!CollectionUtils.isEmpty(tags) && !CollectionUtils.isEmpty(allNowList)) {
            allNowList = allNowList.stream().filter(x -> tags.contains(LeafTag.fromId(x.getFtagId()))).collect(Collectors.toList());
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
     * 处理单个 打标
     *
     * @param stock
     * @return
     */
    private List<UserTagDTO> runAllStockTaskCore(StockBasic stock, LocalDate date) {
        List<UserTagDTO> resultList = new ArrayList<>();

        //涨跌幅
        StockDaily today = stockDailyMapper.selectByTsCodeAndDate(stock.getTsCode(), date);
        //st退市股
        if (today == null) {
            return new ArrayList<>();
        }
        //涨幅
        if (today.getChange().compareTo(BigDecimal.ZERO) > 0) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.UP.getCode(), date));
        } else if (today.getChange().compareTo(BigDecimal.ZERO) < 0) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.DOWN.getCode(), date));
        } else {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.FLAT.getCode(), date));
        }
        //振幅
        BigDecimal todayChange = today.getClose().subtract(today.getOpen());
        if (todayChange.compareTo(BigDecimal.ZERO) > 0) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.YANGXIAN.getCode(), date));

            List<StockDaily> sortedList = stockDailyMapper.selectByTsCodeAndDateRage(stock.getTsCode(), null, date, 2);
            //初始化数据兼容处理
            if (sortedList.size() == 2) {
                StockDaily yesterday = sortedList.get(1);
                if (yesterday.getOpen().compareTo(yesterday.getClose()) > 0 && today.getClose().compareTo(yesterday.getClose()) <= 0) {
                    resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.YANGXIAN_GUXING.getCode(), date));
                }
            }
        } else if (todayChange.compareTo(BigDecimal.ZERO) < 0) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.YINXIAN.getCode(), date));
            List<StockDaily> sortedList = stockDailyMapper.selectByTsCodeAndDateRage(stock.getTsCode(), null, date, 2);
            //初始化数据兼容处理
            if (sortedList.size() == 2) {
                StockDaily yesterday = sortedList.get(1);
                if (yesterday.getClose().compareTo(yesterday.getOpen()) > 0 && today.getClose().compareTo(yesterday.getClose()) >= 0) {
                    resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.YINXIAN_GUXING.getCode(), date));
                }
            }
        }

        //涨跌停
        if (StockLimitUtils.isLimitUp(today)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.ZHANGTING.getCode(), date));
        } else if (StockLimitUtils.isLimitDown(today)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.DIETING.getCode(), date));
        }
        if (StockLimitUtils.touchLimitUp(today)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.TOUCH_ZHANGTING.getCode(), date));
        }

        return resultList;
    }


    private UserTagDTO buildTagRelation(String symbol, String code, LocalDate date) {
        return new UserTagDTO(1L, symbol, tagMap.get(code), date);
    }

}