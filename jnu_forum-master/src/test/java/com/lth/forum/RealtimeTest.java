package com.lth.forum;

import com.lth.forum.enums.LeafTag;
import com.lth.forum.service.DailySchedule;
import com.lth.forum.service.RealtimeTagTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;

@SpringBootTest(classes = JnuForumApplication.class)
class RealtimeTest {
    @Resource
    private RealtimeTagTask realTimeTagTask;
    @Resource
    private DailySchedule dailySchedule;

    @Test
    public void runRealtime() {
        dailySchedule.runRealTime();
        realTimeTagTask.setUserTag("新 华 都", null);
    }
}

