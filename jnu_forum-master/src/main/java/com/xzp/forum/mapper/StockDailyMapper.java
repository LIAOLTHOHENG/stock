package com.xzp.forum.mapper;

import com.xzp.forum.model.StockDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @program: jnu_forum-master
 * @description:
 * @author: Melo
 * @create: 2025-02-25 14:33
 * @Version 1.0
 **/
@Mapper
public interface StockDailyMapper {
    // 单条插入
    int insertStockDaily(StockDaily stockDaily);

    // 批量插入
    int batchInsert(@Param("list") List<StockDaily> list);

    // 按股票代码查询
    List<StockDaily> selectByTsCode(String tsCode);

    // 按日期范围查询
    List<StockDaily> selectByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 按股票代码和日期查询
    StockDaily selectByTsCodeAndDate(@Param("tsCode") String tsCode, @Param("date") LocalDate date);

    List<StockDaily> selectByTsCodeAndDateRage(@Param("tsCode") String tsCode, @Param("start") LocalDate date,
                                               @Param("end") LocalDate end,@Param("count") Integer count);
    // 查询最新记录
    StockDaily selectLatestByTsCode(String tsCode);

    //中位数涨幅
    String getMidIncrese(String date);

    LocalDate getmaxDate();
}
