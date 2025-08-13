package com.lth.forum.mapper;

import com.lth.forum.domain.DailyReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;

/**
* @author Administrator
* @description 针对表【daily_report(日结数据)】的数据库操作Mapper
* @createDate 2025-07-23 16:23:30
* @Entity com.xzp.forum.domain.DailyReport
*/
@Mapper
public interface DailyReportMapper extends BaseMapper<DailyReport> {

    LocalDate getmaxDate();

    void deleteAll();
}




