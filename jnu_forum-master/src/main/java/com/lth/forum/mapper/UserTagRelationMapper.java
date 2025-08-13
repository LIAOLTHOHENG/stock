package com.lth.forum.mapper;

import com.lth.forum.model.CountTagDTO;
import com.lth.forum.model.UserTagDTO;
import com.lth.forum.model.UserTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserTagRelationMapper {
    int insert(UserTagRelation record);
    int batchInsert(List<UserTagRelation> records);
    UserTagRelation selectById(Long id);
    List<UserTagRelation> selectBySymbol(String symbol);
    int update(UserTagRelation record);
    int deleteById(Long id);

    List<UserTagDTO> queryByTagIdsAndSymbol(@Param("list") List<Long> tagIds, String symbol);

    void batchDelete(@Param("list") List<Long> collect);

    //某天某些标签的统计
    List<CountTagDTO> queryByTagAndDate(@Param("tagIds") List<Long> tagIds, @Param("date") LocalDate date);

    LocalDate getMaxDate();

    void deleteAll();
}
