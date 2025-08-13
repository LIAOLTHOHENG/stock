package com.lth.forum.model.api;

import lombok.Data;
import java.util.Map;

@Data
public class TushareReq {
    // 接口名称（如：daily）
    private String api_name;

    // 用户唯一标识token
    private String token;

    // 接口参数键值对（示例：start_date=20230101&end_date=20231231）
    private Map<String, Object> params;

    // 需要返回的字段列表（示例："open,high,low,close"）
    private String fields;
}
