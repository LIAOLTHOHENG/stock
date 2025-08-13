package com.lth.forum.model;

import lombok.Data;

/**
 * @program: jnu_forum-master
 * @description:
 * @author: Melo
 * @create: 2025-02-25 16:24
 * @Version 1.0
 **/
@Data
public class StockBasic {
    public String tsCode;
    public String symbol;
    public String name;
    public String industry;
    public String area;
    private Double totalMarketCap;   // 总市值
    private Double floatMarketCap;   // 流通市值
}