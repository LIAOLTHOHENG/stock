package com.lth.forum;

import com.lth.forum.mapper.DailyReportMapper;
import com.lth.forum.mapper.StockDailyMapper;
import com.lth.forum.mapper.UserTagRelationMapper;
import com.lth.forum.service.DailySchedule;
import com.lth.forum.service.EastMoneyCrawler;
import com.lth.forum.service.TagTask;
import com.lth.forum.util.StockUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SpringBootTest(classes = JnuForumApplication.class)
class UtilTest {

    //测试成交量估算
    @Test
    void testEstimateDailyVolume(){
        //System.out.println(StockUtil.estimateDailyVolume(LocalDateTime.of(2025, 7, 30, 12, 01), 3664));
        System.out.println(StockUtil.estimateDailyVolume(LocalDateTime.of(2025, 7, 30, 13, 01), 3664));
    }

}
