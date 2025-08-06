package com.xzp.forum.mapper;

import com.xzp.forum.model.StocksByIndustry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 日常使用mapper
 */
@Mapper
public interface NormalMapper {

    //实时
    List<StocksByIndustry> getRealtimeStocksByIndustry(@Param("tagIds") List<Long> tagIds);

    //实时严格方式
    List<StocksByIndustry> getRealtimeStocksByIndustryStrict(@Param("days") int days,
                                                             @Param("frequency") int frequency, @Param("tagIds") List<Long> tagIds);

    //5天
    List<StocksByIndustry> getAfter3StocksByIndustry(@Param("date")LocalDate  date,@Param("tagIds") List<Long> tagIds);

    //5天严格方式
    List<StocksByIndustry> getAfter3StocksByIndustryStrict(@Param("date")LocalDate  date,@Param("days") int days,
                                                           @Param("frequency") int frequency, @Param("tagIds") List<Long> tagIds);
}




