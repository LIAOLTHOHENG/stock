package com.xzp.forum;

import com.xzp.forum.enums.LeafTag;
import com.xzp.forum.service.DailySchedule;
import com.xzp.forum.service.RealtimeTagTask;
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
        realTimeTagTask.setUserTag(null, Arrays.asList(LeafTag.YANGXIAN_GUXING));
    }
}

