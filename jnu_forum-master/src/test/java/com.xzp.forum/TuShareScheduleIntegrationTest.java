package com.xzp.forum;

import com.xzp.forum.service.EastMoneyCrawler;
import com.xzp.forum.service.TagTask;
import com.xzp.forum.service.DailySchedule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SpringBootTest(classes = JnuForumApplication.class)
class TuShareScheduleIntegrationTest {

    @Resource
    private DailySchedule dailySchedule;
    @Resource
    private EastMoneyCrawler eastMoneyCrawler;

    /**
     * 基础信息
     */
    @Test
    void insertInfo() {
        eastMoneyCrawler.init();
    }
    @Test
    void forNewEnv() {

        // 重跑7月2日到7月25日的交易日的标签数据
        LocalDate startDate = LocalDate.of(2025, 7, 2);
        LocalDate endDate = LocalDate.of(2025, 7, 25);
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 只处理工作日（周一至周五）
            if (currentDate.getDayOfWeek().getValue() <= 5) {

                try {
                    String tradeDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
                    dailySchedule.getAllStockDaily(tradeDate);
                    tagTask.setUserTag(null, currentDate);
                    dailySchedule.runDailyReport(tradeDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 防止请求过频，适当休眠
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {}
            }

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
    }


    @Resource
    TagTask tagTask;

    @Test
    void tag() {
        tagTask.setUserTag(null, LocalDate.of(2025, 7, 17));
    }

    @Test
    void task() {
        dailySchedule.getAllStockDaily("20250725");
        tagTask.setUserTag(null, LocalDate.of(2025, 7, 25));
        dailySchedule.runDailyReport("20250725");
    }

    @Test
    void reRunTag() {
        // 重跑7月2日到7月25日的交易日的标签数据
        LocalDate startDate = LocalDate.of(2025, 7, 2);
        LocalDate endDate = LocalDate.of(2025, 7, 25);
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 只处理工作日（周一至周五）
            if (currentDate.getDayOfWeek().getValue() <= 5) {

                try {
                    tagTask.setUserTag(null, currentDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 防止请求过频，适当休眠
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {}
            }

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
    }


    @Test
    void reRunReport() {
        // 重跑7月2日到7月23日的交易日的标签数据 & 补齐7月2号到22号的日报
        LocalDate startDate = LocalDate.of(2025, 7, 2);
        LocalDate endDate = LocalDate.of(2025, 7, 25);
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            // 只处理工作日（周一至周五）
            if (currentDate.getDayOfWeek().getValue() <= 5) {
                String tradeDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);

                try {
                    dailySchedule.runDailyReport(tradeDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 防止请求过频，适当休眠
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignored) {}
            }

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }
    }

}
