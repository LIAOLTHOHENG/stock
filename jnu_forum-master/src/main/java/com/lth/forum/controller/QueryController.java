package com.lth.forum.controller;

import com.lth.forum.enums.LeafTag;
import com.lth.forum.mapper.NormalMapper;
import com.lth.forum.mapper.UserTagRelationMapper;
import com.lth.forum.model.StocksByIndustry;
import com.lth.forum.service.DailySchedule;
import com.lth.forum.service.RealtimeTagTask;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
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
        return normalMapper.getRealtimeStocksByIndustry(new ArrayList<>() {{
            add(LeafTag.YANGXIAN_GUXING.getId());
        }});
    }

    /**
     * 获取实时数据 v2 新增了标签
     *
     * @return
     */
    @PostMapping("/realtime/v2")
    public List<StocksByIndustry> realtimev2() {
        dailySchedule.runRealTime();
        realTimeTagTask.setUserTag(null, Arrays.asList(LeafTag.YANGXIAN_GUXING, LeafTag.UP_HUG,LeafTag.UP_INSERTION));
        return normalMapper.getRealtimeStocksByIndustry(LeafTag.getUpTags().stream().map(LeafTag::getId).collect(Collectors.toList()));
    }

    /**
     * 获取实时数据 严格版
     *
     * @return
     */
    @PostMapping("/realtimestrict")
    public List<StocksByIndustry> realtimestrict(@RequestBody RealtimeStrict req) {
        dailySchedule.runRealTime();
        realTimeTagTask.setUserTag(null, Arrays.asList(LeafTag.YANGXIAN_GUXING, LeafTag.UP_HUG, LeafTag.UP_INSERTION));
        List<Long> upTagIds = LeafTag.getUpTags().stream().map(LeafTag::getId).collect(Collectors.toList());
        List<StocksByIndustry> stocks = normalMapper.getRealtimeStocksByIndustryStrict(req.getDays(), req.getFrequency(), upTagIds);
        return stocks;
    }

    /**
     * 获取盘后数据
     *
     * @return
     */
    @PostMapping("/after3")
    public List<StocksByIndustry> after3(@RequestBody RealtimeStrict req) {
        List<StocksByIndustry> stocks = normalMapper.getAfter3StocksByIndustry(LocalDate.now(), new ArrayList<>() {{
            add(LeafTag.YANGXIAN_GUXING.getId());
        }});
        return stocks;
    }

    /**
     * 获取盘后数据 多标签
     *
     * @return
     */
    @PostMapping("/after3/v2")
    public List<StocksByIndustry> after3v2(@RequestBody RealtimeStrict req) {
        List<StocksByIndustry> stocks = normalMapper.getAfter3StocksByIndustry(LocalDate.now(), LeafTag.getUpTags().stream().map(LeafTag::getId).collect(Collectors.toList()));
        return stocks;
    }

    /**
     * 获取盘后数据 严格版
     *
     * @return
     */
    @PostMapping("/after3strict")
    public List<StocksByIndustry> after3strict(@RequestBody RealtimeStrict req) {
        List<Long> upTagIds = LeafTag.getUpTags().stream().map(LeafTag::getId).collect(Collectors.toList());
        List<StocksByIndustry> stocks = normalMapper.getAfter3StocksByIndustryStrict(LocalDate.now(), req.getDays(), req.getFrequency(), upTagIds);
        return stocks;
    }

    /**
     * 刷新数据
     * @param req
     * @return
     */
    @GetMapping("/fresh_realtime")
    public String afrshRealTime() {
        //dailySchedule.runRealTime();
        List<LeafTag> tags =  new ArrayList<>(LeafTag.getUpTags());
        tags.add(LeafTag.STABLE_VOLUME);
        realTimeTagTask.setUserTag(null, tags);
        return "done";
    }

    @Data
    static class RealtimeStrict {
        private int days = 5;
        private int frequency = 1;
    }
}
