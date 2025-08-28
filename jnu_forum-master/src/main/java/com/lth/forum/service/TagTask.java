package com.lth.forum.service;

import com.google.common.collect.Lists;
import com.lth.forum.enums.LeafTag;
import com.lth.forum.model.StockBasic;
import com.lth.forum.mapper.StockBasicMapper;
import com.lth.forum.mapper.StockDailyMapper;
import com.lth.forum.mapper.UserTagRelationMapper;
import com.lth.forum.model.StockDaily;
import com.lth.forum.model.UserTagDTO;
import com.lth.forum.model.UserTagRelation;
import com.lth.forum.util.StockLimitUtils;
import com.lth.forum.util.StockTagUtils;
import com.lth.forum.util.StockUtil;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private void dealTagByAll(String name, LocalDate date, List<LeafTag> tags) {
        List<UserTagDTO> allNowList = Collections.synchronizedList(new ArrayList<>());
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
                            System.out.println("error" + "-" + user.getSymbol() + "-" + date);
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
        dealData(allNowList, name, date);
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
        List<StockDaily> sortedList = stockDailyMapper.selectByTsCodeAndDateRage(stock.getTsCode(), null, date, 6);
        StockDaily today = null;
        if (sortedList.size() > 0) {
            today = sortedList.get(0);
        }
        //st退市股
        if (today == null) {
            return new ArrayList<>();
        }
        //涨幅标签处理
        StockTagUtils.processChangeTags(today, (symbol, code) -> {
            resultList.add(buildTagRelation(symbol, code, date));
        }, stock.getSymbol());
        
        StockDaily yesterday = null;
        if (!CollectionUtils.isEmpty(sortedList) && sortedList.size() > 1) {
            yesterday = sortedList.get(1);
        }
        
        // 处理共同的标签逻辑
        StockTagUtils.processCommonTags(stock, today, yesterday, sortedList, (symbol, code) -> {
            resultList.add(buildTagRelation(symbol, code, date));
        });

        //涨跌停 (仅盘后任务有)
        if (StockLimitUtils.isLimitUp(today)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.ZHANGTING.getCode(), date));
        } else if (StockLimitUtils.isLimitDown(today)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.DIETING.getCode(), date));
        }
        if (StockLimitUtils.touchLimitUp(today)) {
            resultList.add(buildTagRelation(stock.getSymbol(), LeafTag.TOUCH_ZHANGTING.getCode(), date));
        }
        StockTagUtils.processUserTagList(resultList);
        return resultList;
    }


    private UserTagDTO buildTagRelation(String symbol, String code, LocalDate date) {
        return new UserTagDTO(1L, symbol, tagMap.get(code), date);
    }

}