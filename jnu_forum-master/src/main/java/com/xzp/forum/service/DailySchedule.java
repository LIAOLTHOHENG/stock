package com.xzp.forum.service;

import com.xzp.forum.mapper.StockDailyMapper;
import com.xzp.forum.model.StockDaily;
import com.xzp.forum.model.api.TushareReq;
import com.xzp.forum.model.api.TushareResp;
import com.xzp.forum.util.StockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @program: jnu_forum-master
 * @description:
 * @author: Melo
 * @create: 2025-02-24 20:00
 * @Version 1.0
 **/
@Component
public class DailySchedule {

    @Value("${tushare.token}")
    private String token;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private StockDailyMapper stockDailyMapper;
    private static final String TUSHARE_URL = "http://api.tushare.pro";
    private final DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 每日数据拉取
     * @param date
     */
    public void getAllStockDaily(String date) {
        if (date == null) {
            date = LocalDate.now().format(formatter1);
        }
        String method = "daily";
        TushareReq tushareReq = new TushareReq();
        tushareReq.setApi_name(method);
        tushareReq.setToken(token);
        tushareReq.setFields("ts_code,trade_date,open,high,low,close,pre_close,change,pct_chg,vol,amount");
        String finalDate = date;
        tushareReq.setParams(new HashMap<>() {{
            put("start_date", finalDate);
            put("end_date", finalDate);
        }});

        // 3. 发送POST请求
        TushareResp response = restTemplate.postForObject(
                TUSHARE_URL,
                tushareReq,
                TushareResp.class
        );

        // 4. 处理响应（根据实际需求补充）
        if (response != null && response.getCode() == 0) {
            // 处理成功响应
            System.out.println("获取到" + response.getData().getItems().size() + "条数据");
        } else {
            // 处理错误
            String errorMsg = response != null ? response.getMsg() : "请求失败";
            System.err.println("API调用失败: " + errorMsg);
        }
        // 5. 循环处理处理 插入 每次500条
        for (int i = 0; i < response.getData().getItems().size(); i += 500) {
            int endIndex = Math.min(i + 500, response.getData().getItems().size());
            List<StockDaily> list = buildStockDailyDOList(response.getData().getItems().subList(i, endIndex));
            try {
                stockDailyMapper.batchInsert(list);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("插入失败" + list);
            }
        }

    }
    private List<StockDaily> buildStockDailyDOList(List<List<Object>> subList) {
        List<StockDaily> stockDailyList = new ArrayList();
        //根据 Fields里面的字段 设置进数据库对象
        for (List<Object> objects : subList) {

            // 过滤不符合条件的股票
            if (StockUtil.isRestrictedStock(objects.get(0).toString())) {
                continue;
            }
            StockDaily stockDaily = new StockDaily();
            stockDaily.setTsCode(objects.get(0).toString());
            stockDaily.setTradeDate(LocalDate.parse(objects.get(1).toString(), formatter1));
            stockDaily.setOpen(new BigDecimal(objects.get(2).toString()));
            stockDaily.setHigh(new BigDecimal(objects.get(3).toString()));
            stockDaily.setLow(new BigDecimal(objects.get(4).toString()));
            stockDaily.setClose(new BigDecimal(objects.get(5).toString()));
            stockDaily.setPreClose(new BigDecimal(objects.get(6).toString()));
            stockDaily.setChange(new BigDecimal(objects.get(7).toString()));
            stockDaily.setPctChg(new BigDecimal(objects.get(8).toString()));
            stockDaily.setVol(new BigDecimal(objects.get(9).toString()));
            stockDaily.setAmount(new BigDecimal(objects.get(10).toString()));
            stockDailyList.add(stockDaily);
        }
        return stockDailyList;
    }

    /**
     * 每日数据报告
     * @param date
     */
    public void runDailyReport(String date) {
        String mid = stockDailyMapper.getMidIncrese(date);
        System.out.println("中位数涨幅:"+mid);
    }

    @PostConstruct
    public void init() {
        System.out.println("TuShareSchedule init" + token);
    }
}