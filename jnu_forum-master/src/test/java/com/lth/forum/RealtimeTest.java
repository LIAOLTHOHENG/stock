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
        realTimeTagTask.setUserTag("五洲新春", Arrays.asList(LeafTag.YANGXIAN_GUXING, LeafTag.UP_INSERTION,
                LeafTag.UP_HUG, LeafTag.UP_YUNXIAN,LeafTag.STABLE_VOLUME));
    }
}

