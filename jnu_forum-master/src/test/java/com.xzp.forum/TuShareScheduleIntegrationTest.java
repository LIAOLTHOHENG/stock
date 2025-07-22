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
     * 某天
     */
    @Test
    void shouldReturnValidDataStructure() {
        LocalDate date = LocalDate.now();
        dailySchedule.getAllStockDaily("20250717");
    }

    /**
     * 过去一年
     *
     * @throws InterruptedException
     */
    @Test
    void lastWeekData() throws InterruptedException {
        // 获取当前日期和一年前日期
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusWeeks(2);

        // 遍历日期范围
        LocalDate currentDate = endDate;
        while (currentDate.isAfter(startDate) || currentDate.isEqual(startDate)) {
            // 仅处理工作日（周一至周五）
            if (currentDate.getDayOfWeek().getValue() <= 5) { // 1=Monday,5=Friday
                String tradeDate = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
                System.out.println("Processing date: " + tradeDate);

                try {
                    dailySchedule.getAllStockDaily(tradeDate);
                } catch (Exception e) {
                    System.err.println("处理日期 " + tradeDate + " 时发生异常：" + e.getMessage());
                    e.printStackTrace();
                }

                // 休眠150ms防止请求过频  频率 1分钟600次以内
                Thread.sleep(150);
            }

            // 倒序处理日期（从最近往过去处理）
            currentDate = currentDate.minusDays(1);
        }
    }

    /**
     * 基础信息
     */
    @Test
    void insertInfo() {
        eastMoneyCrawler.init();
    }

    @Resource
    TagTask tagTask;

    @Test
    void tag() {
        tagTask.setUserTag(null, LocalDate.of(2025, 7, 17));
    }

    @Test
    void task() {
        dailySchedule.getAllStockDaily("20250722");
        tagTask.setUserTag(null, LocalDate.of(2025, 7, 22));
        dailySchedule.runDailyReport("20250722");
    }
}
