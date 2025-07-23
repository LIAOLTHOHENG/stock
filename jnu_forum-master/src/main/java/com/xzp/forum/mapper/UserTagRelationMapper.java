package com.xzp.forum.mapper;

import com.xzp.forum.model.CountTagDTO;
import com.xzp.forum.model.UserTagDTO;
import com.xzp.forum.model.UserTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    List<CountTagDTO> queryByTagAndDate(@Param("tagIds") List<Long> tagIds, @Param("date") String date);
}
