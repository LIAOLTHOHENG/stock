package com.lth.forum.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @program: yqtrack.java.ims.backend
 * @description: 用户标签映射
 * @author: Melo
 * @create: 2024-12-21 10:32
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTagRealtimeDTO {
    private long id;

    private String symbol;

    private Long FtagId;

}