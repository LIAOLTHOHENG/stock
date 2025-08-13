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
public class UserTagDTO {
    private long id;

    private String symbol;

    private Long FtagId;

    private LocalDate date;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTagDTO)) return false;
        UserTagDTO that = (UserTagDTO) o;
        return symbol.equals(that.symbol) && FtagId.equals(that.FtagId) && date.equals(that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, FtagId, date);
    }
}