package com.xzp.forum.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 标签关系表(实时)
 * @TableName user_tag_relation_realtime
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private LocalDateTime fcreatetime;

    /**
     * 
     */
    private String description;

}