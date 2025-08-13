package com.lth.forum.model;

import lombok.Data;

@Data
public class StocksByIndustry {
    private String industry;

    private String totalAmountWithUnit;

    private String names;

    private Integer tagCount;
}
