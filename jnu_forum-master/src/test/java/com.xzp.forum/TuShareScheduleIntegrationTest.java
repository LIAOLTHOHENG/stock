package com.xzp.forum;

import com.xzp.forum.mapper.DailyReportMapper;
import com.xzp.forum.mapper.StockDailyMapper;
import com.xzp.forum.mapper.UserTagRelationMapper;
import com.xzp.forum.enums.LeafTag;
import com.xzp.forum.service.EastMoneyCrawler;
import com.xzp.forum.service.TagTask;
import com.xzp.forum.service.DailySchedule;
import com.xzp.forum.util.StockUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@SpringBootTest(classes = JnuForumApplication.class)
class TuShareScheduleIntegrationTest {

    @Resource
    private DailySchedule dailySchedule;
    @Resource
    private EastMoneyCrawler eastMoneyCrawler;
    @Resource
    StockDailyMapper stockDailyMapper;
    @Resource
    UserTagRelationMapper userTagRelationMapper;
    @Resource
    DailyReportMapper dailyReportMapper;

    /**
     * 日常任务
     */
    @Test
    void normalTask() {
        eastMoneyCrawler.init();
        dailySchedule.getAllStockDaily(null);
        tagTask.setUserTag(null, null, null);
        dailySchedule.runDailyReport(null);
    }

    /**
     * 基础信息
     */
    @Test
    void insertInfo() {
        eastMoneyCrawler.init();
    }

    /**
     * 打标单测
     */
    @Test
    void tag() {
        tagTask.setUserTag(null, LocalDate.of(2025, 7, 30), null);
    }

    /**
     * 新环境初始化
     */
    @Test
    void forNewEnv() {
        stockDailyMapper.deleteAll();
        userTagRelationMapper.deleteAll();
        dailyReportMapper.deleteAll();

        // 重跑7月2日到7月25日的交易日的标签数据
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.now();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 只处理工作日（周一至周五）
            if (currentDate.getDayOfWeek().getValue() <= 5) {

                try {
                    String tradeDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
                    dailySchedule.getAllStockDaily(tradeDate);
                    tagTask.setUserTag(null, currentDate, null);
                    dailySchedule.runDailyReport(tradeDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 防止请求过频，适当休眠
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {
                }
            }
            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
    }
    @Resource
    TagTask tagTask;

    @Test
    void task() {
        eastMoneyCrawler.init();
        dailySchedule.getAllStockDaily(null);
        tagTask.setUserTag(null, null,null);
        dailySchedule.runDailyReport(null);
    }

    @Test
    void forDebugTag() {
        userTagRelationMapper.deleteAll();
        dailyReportMapper.deleteAll();

        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.now();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 只处理工作日（周一至周五）
            if (currentDate.getDayOfWeek().getValue() <= 5) {

                try {
                    String tradeDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
                    tagTask.setUserTag(null, currentDate, null);
                    dailySchedule.runDailyReport(tradeDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
    }

    //测试成交量估算
    @Test
    void testEstimateDailyVolume(){
        //System.out.println(StockUtil.estimateDailyVolume(LocalDateTime.of(2025, 7, 30, 12, 01), 3664));
        System.out.println(StockUtil.estimateDailyVolume(LocalDateTime.of(2025, 7, 30, 13, 01), 3664));
    }

}
