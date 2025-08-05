package com.xzp.forum.mapper;

import com.xzp.forum.domain.StockRealtime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Administrator
* @description 针对表【stock_realtime(股票日线实时数据表)】的数据库操作Mapper
* @createDate 2025-08-04 22:30:58
* @Entity com.xzp.forum.domain.StockRealtime
*/
@Mapper
public interface StockRealtimeMapper {

    int deleteByPrimaryKey(Long id);

    int insertSelective(StockRealtime record);

    StockRealtime selectByPrimaryKey(Long id);

    void batchInsert(@Param("list") List<StockRealtime>  records);

    StockRealtime selectByTsCode(@Param("tsCode") String tsCode);

    void deleteAll();
}
