package com.xzp.forum.service;

import com.google.common.collect.Lists;
import com.xzp.forum.domain.StockRealtime;
import com.xzp.forum.domain.UserTagRelationRealtime;
import com.xzp.forum.enums.LeafTag;
import com.xzp.forum.mapper.*;
import com.xzp.forum.model.StockBasic;
import com.xzp.forum.model.StockDaily;
import com.xzp.forum.model.UserTagDTO;
import com.xzp.forum.model.UserTagRealtimeDTO;
import com.xzp.forum.model.UserTagRelation;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
@Component("RealtimeTagTask")
public class RealtimeTagTask {
    private Map<String, Long> tagMap = new HashMap();
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private int corePoolSize = 8;
    /**
     * 最大可创建的线程数
     */
    private int maxPoolSize = 8;

    @Resource
    private StockBasicMapper stockBasicMapper;
    @Resource
    private UserTagRelationRealtimeMapper userTagRelationRealtimeMapper;
    @Resource
    private StockRealtimeMapper stockRealtimeMapper;
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
    public void setUserTag(String name, List<LeafTag> tags) {
        try {
            System.out.println("TagTaskstart");
            dealTagByAll(name, tags);
        } finally {
            System.out.println("TagTaskend");
        }
    }


    /**
     * 所有用户口径
     */
    private void dealTagByAll(String name, List<LeafTag> tags) {
        List<UserTagRealtimeDTO> allNowList = Collections.synchronizedList(new ArrayList<>());
        Long total = stockBasicMapper.count(name);
        //防止深分页
        String lastSymbol = null;
        //循环次数
        Long loopNum = total / PAGE_SIZE + 1;
        for (int i = 1; i <= loopNum; i++) {
            List<StockBasic> stockList = stockBasicMapper.getByPageSize(lastSymbol, PAGE_SIZE, name);
            if (CollectionUtils.isEmpty(stockList)) {
                continue;
            }
            CountDownLatch countDownLatch = new CountDownLatch(stockList.size());
            lastSymbol = stockList.get(stockList.size() - 1).getName();
            for (StockBasic user : stockList) {
                List<UserTagRealtimeDTO> finalAllNowList = allNowList;
                threadPoolTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //跑出当前标签
                            List<UserTagRealtimeDTO> nowList = runAllStockTaskCore(user);
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
        dealData(allNowList);
    }


    /**
     * 数据库操作
     *
     * @param nowList
     */
    private void dealData(List<UserTagRealtimeDTO> nowList) {
        userTagRelationRealtimeMapper.deleteAll();
        if (!CollectionUtils.isEmpty(nowList)) {
            List<List<UserTagRealtimeDTO>> partitionList = Lists.partition(nowList, 500);
            for (List<UserTagRealtimeDTO> list : partitionList) {
                userTagRelationRealtimeMapper.batchInsert(list.stream().map(x -> {
                    return new UserTagRelationRealtime(null, x.getSymbol(), x.getFtagId(), LocalDateTime.now(), LeafTag.fromId(x.getFtagId()).getDescription());
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
    private List<UserTagRealtimeDTO> runAllStockTaskCore(StockBasic stock) {
        try {
            List<UserTagRealtimeDTO> resultList = new ArrayList<>();
            //涨跌幅
            StockRealtime realTime = stockRealtimeMapper.selectByTsCode(stock.getTsCode());
            //st退市股
            if (realTime == null) {
                return new ArrayList<>();
            }
            //振幅
            BigDecimal todayChange = realTime.getClose().subtract(realTime.getOpen());
            if (todayChange.compareTo(BigDecimal.ZERO) > 0) {
                //实时任务里 查最近一条肯定是昨天的（因为今天的盘后才更新）
                List<StockDaily> sortedList = stockDailyMapper.selectByTsCodeAndDateRage(stock.getTsCode(), null, LocalDate.now(), 1);
                if (sortedList.size() == 1) {
                    StockDaily yesterday = sortedList.get(0);
                    if (yesterday.getOpen().compareTo(yesterday.getClose()) > 0 && realTime.getClose().compareTo(yesterday.getClose()) <= 0) {
                        resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.YANGXIAN_GUXING.getCode()));
                    }
                }
            } else if (todayChange.compareTo(BigDecimal.ZERO) < 0) {
                List<StockDaily> sortedList = stockDailyMapper.selectByTsCodeAndDateRage(stock.getTsCode(), null, LocalDate.now(), 1);
                if (sortedList.size() == 1) {
                    StockDaily yesterday = sortedList.get(0);
                    if (yesterday.getClose().compareTo(yesterday.getOpen()) > 0 && realTime.getClose().compareTo(yesterday.getClose()) >= 0) {
                        resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.YINXIAN_GUXING.getCode()));
                    }
                }
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error" + stock.getSymbol() + stock.getName());
        }
        return new ArrayList<>();
    }


    private UserTagRealtimeDTO buildTagRelation(String symbol, String code) {
        return new UserTagRealtimeDTO(1L, symbol, tagMap.get(code));
    }

}