package com.xzp.forum.model.api;

import lombok.Data;

import java.util.List;

/**
 * Tushare API响应数据封装类
 *
 * @program: jnu_forum-master
 * @description: 封装Tushare API接口返回的响应数据
 * @author: Melo
 * @create: 2025-02-24 20:08
 * @Version 1.0
 **/
@Data
public class TushareResp {

    /**
     * 响应状态码
     * - 0：请求成功
     * - 其他：请求失败，具体错误码参考Tushare文档
     */
    private Integer code;

    /**
     * 响应消息
     * - 请求成功时通常为"ok"
     * - 请求失败时为具体的错误信息
     */
    private String msg;

    /**
     * 响应数据主体
     * - 数据结构根据不同的API接口返回
     * - 通常包含字段列表（fields）和数据项（items）
     */
    private RespData data;

    @Data
    public static class RespData {
        private List<String> fields;

        private List<List<Object>> items;
    }
}
