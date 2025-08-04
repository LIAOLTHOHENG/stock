package com.xzp.forum.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xzp.forum.mapper.StockBasicMapper;
import com.xzp.forum.model.StockBasic;
import com.xzp.forum.util.StockUtil;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 拉数据相关接口
 */
@Component
public class EastMoneyCrawler {
    @Resource
    private StockBasicMapper stockBasicMapper;

    public void init() {
        List<StockBasic> stockList = new ArrayList<>();
        final int pageSize = 100;
        int total = 0;

        try {
            // 第一次请求获取总数量
            JSONObject firstPage = fetchData(1, 1);
            total = Integer.parseInt(firstPage.getJSONObject("data").get("total").toString());
            System.out.println("总数据量：" + total);

            // 计算需要请求的页数
            int totalPages = (total + pageSize - 1) / pageSize;

            // 分页请求
            for (int page = 1; page <= totalPages; page++) {
                JSONObject response = fetchData(page, pageSize);
                JSONArray data = response.getJSONObject("data").getJSONArray("diff");
                parseData(data, stockList);
                stockBasicMapper.batchInsert(stockList);
                System.out.printf("已获取第%d页数据，当前总数：%d%n", page, stockList.size());
            }

            stockList.stream().forEach(System.out::println);
            System.out.println("共获取股票数量：" + stockList.size());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject fetchData(int page, int pageSize) throws IOException {
        String apiUrl = "http://23.push2.eastmoney.com/api/qt/clist/get?"
                + "pn=" + page
                + "&pz=" + pageSize
                + "&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281"
                + "&fltt=2&invt=2&fid=f3"
                + "&fs=m:0+t:6,m:0+t:13,m:0+t:80,m:1+t:2,m:1+t:23"
                + "&fields=f12,f14,f100,f20,f21,f22,f23,f107,f140,f152,f228";

        String jsonStr = Jsoup.connect(apiUrl)
                .ignoreContentType(true)
                .timeout(60000)
                .execute().body();
        return JSONObject.parseObject(jsonStr);
    }

    private static void parseData(JSONArray data, List<StockBasic> stockList) {
        for (int i = 0; i < data.size(); i++) {
            JSONObject item = data.getJSONObject(i);
            StockBasic stock = new StockBasic();
            // 过滤不符合条件的股票
            if (StockUtil.isRestrictedStock(item.getString("f12"))) {
                continue;
            }
            // 必填字段解析
            stock.symbol = item.getString("f12");  // 股票代码
            stock.name = item.getString("f14");
            try {
                Double y = item.getDouble("f20");
                Double x = item.getDouble("f21");
                stock.setTotalMarketCap(y);
                stock.setFloatMarketCap(x);
            } catch (Exception e) {
                System.out.println("获取市值失败" + stock.symbol + item.getString("f20") + ";" + item.getString("f21"));
            }
            // 股票名称
            // 处理可能缺失的字段
            stock.industry = item.getString("f100") != null ?
                    item.getString("f100") : "未知行业";
            stock.area = item.getString("f162") != null ?
                    item.getString("f162") : "未知地区";

            // 生成ts_code（根据股票代码判断市场）
            stock.tsCode = generateTsCode(stock.symbol);

            stockList.add(stock);
        }
    }

    // 添加ts_code生成方法
    private static String generateTsCode(String symbol) {
        if (symbol.startsWith("6")) {
            return symbol + ".SH"; // 沪市
        } else if (symbol.startsWith("0") || symbol.startsWith("3")) {
            return symbol + ".SZ"; // 深市
        } else if (symbol.startsWith("4") || symbol.startsWith("8")) {
            return symbol + ".BJ"; // 北交所
        }
        return symbol + ".UNKNOWN";
    }

}