package com.xzp.forum.mapper;

import com.xzp.forum.model.StockBasic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockBasicMapper {
    int insert(StockBasic record);

    void batchInsert(@Param("list") List<StockBasic> record);

    int deleteByTsCode(@Param("tsCode") String tsCode);

    int update(StockBasic record);

    StockBasic selectByTsCode(@Param("tsCode") String tsCode);

    StockBasic selectBySymbol(@Param("symbol") String symbol);

    List<StockBasic> selectAll();

    List<StockBasic> selectByIndustry(@Param("industry") String industry);


    List<StockBasic> getByPageSize(@Param("lastName")String lastName, @Param("pageSize")Integer pageSize,@Param("name") String name);

    Long count(@Param("name") String name);
}
