package com.xzp.forum.controller;

import com.google.common.collect.Lists;
import com.xzp.forum.enums.LeafTag;
import com.xzp.forum.mapper.NormalMapper;
import com.xzp.forum.mapper.UserTagRelationMapper;
import com.xzp.forum.model.StocksByIndustry;
import com.xzp.forum.service.DailySchedule;
import com.xzp.forum.service.RealtimeTagTask;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class QueryController {

    @Resource
    private NormalMapper normalMapper;
    @Resource
    private RealtimeTagTask realTimeTagTask;
    @Resource
    private DailySchedule dailySchedule;
    @Autowired
    private UserTagRelationMapper userTagRelationMapper;

    /**
     * 获取实时数据
     *
     * @return
     */
    @PostMapping("/realtime")
    public List<StocksByIndustry> realtime() {
        dailySchedule.runRealTime();
        realTimeTagTask.setUserTag(null, Arrays.asList(LeafTag.YANGXIAN_GUXING));
        return normalMapper.getStocksByIndustry();
    }

    /**
     * 获取实时数据 严格版
     *
     * @return
     */
    @PostMapping("/realtimestrict")
    public List<StocksByIndustry> realtimestrict(@RequestBody RealtimeStrict req) {
        dailySchedule.runRealTime();
        realTimeTagTask.setUserTag(null, Arrays.asList(LeafTag.YANGXIAN_GUXING));
        List<Long> upTagIds = LeafTag.getUpTags().stream().map(LeafTag::getId).collect(Collectors.toList());
        List<StocksByIndustry> stocks = normalMapper.getStocksByIndustryStrict(req.getDays(), req.getFqc(), upTagIds);

        return stocks;
    }

    @Data
    static class RealtimeStrict {
        private int days = 5;
        private int fqc = 1;
    }
}
