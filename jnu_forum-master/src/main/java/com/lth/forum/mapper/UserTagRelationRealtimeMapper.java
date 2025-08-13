package com.lth.forum.mapper;

import com.lth.forum.domain.UserTagRelationRealtime;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【user_tag_relation_realtime(标签关系表(实时))】的数据库操作Mapper
 * @createDate 2025-08-04 22:44:39
 * @Entity com.xzp.forum.domain.UserTagRelationRealtime
 */
@Mapper
public interface UserTagRelationRealtimeMapper extends BaseMapper<UserTagRelationRealtime> {

    void deleteAll();

    void batchInsert(@Param("list") List<UserTagRelationRealtime> nowList);
}




