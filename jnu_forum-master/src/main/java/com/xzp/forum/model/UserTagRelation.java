package com.xzp.forum.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
*
*  @author melo
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTagRelation {

    /**
    * 主键
    * 主键
    * isNullAble:0
    */
    private Long id;


    private String symbol;
    /**
    * 标签ID
    * isNullAble:0
    */
    private Long FTagId;

    /**
    * 创建时间
    * isNullAble:0,defaultVal:CURRENT_TIMESTAMP(3)
    */
    private java.time.LocalDateTime FCreateTime;

    private LocalDate date;


    private String description;

}
