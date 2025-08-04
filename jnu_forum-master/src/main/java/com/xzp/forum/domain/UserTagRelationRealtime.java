package com.xzp.forum.domain;

import lombok.Data;

import java.util.Date;

/**
 * 标签关系表(实时)
 * @TableName user_tag_relation_realtime
 */
@Data
public class UserTagRelationRealtime {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 代码
     */
    private String symbol;

    /**
     * 标签ID
     */
    private Long ftagid;

    /**
     * 创建时间
     */
    private Date fcreatetime;

    /**
     * 
     */
    private String description;

}