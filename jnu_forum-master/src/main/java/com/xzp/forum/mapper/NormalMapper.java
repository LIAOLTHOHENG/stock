package com.xzp.forum.mapper;

import com.xzp.forum.model.StocksByIndustry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 日常使用mapper
 */
@Mapper
public interface NormalMapper {


    List<StocksByIndustry> getStocksByIndustry();

    List<StocksByIndustry> getStocksByIndustryStrict(@Param("days") int days,
                                                     @Param("frequency") int frequency, @Param("tagIds") List<Long> tagIds);
}




