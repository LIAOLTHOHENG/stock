package com.lth.forum.enums;


import java.util.ArrayList;
import java.util.List;

/**
 * 股票特性标签 叶子结点枚举
 */
public enum LeafTag {


    _10CM("10CM", "10厘米", 1L, null),
    _20CM("20CM", "20厘米", 2L, null),
    CAN_DEAL("CAN_DEAL", "属于创业板和主板", 3L, null),
    //缩量至平稳
    STABLE("STABLE", "缩量至平稳", 4L, null),
    //跌幅实体越来越小
    DOWN_ENTITY("DOWN_ENTITY", "跌幅实体越来越小", 8L, null),
    //涨幅大于0
    UP("UP", "涨幅大于0", 9L, null),
    //跌幅大于0
    DOWN("DOWN", "跌幅大于0", 10L, null),
    //涨幅等于0
    FLAT("FLAT", "涨幅等于0", 11L, null),

    ZHANGTING("ZHANGTING", "涨停", 12L, null),

    DIETING("DIETING", "跌停", 13L, null),

    TOUCH_ZHANGTING("TOUCH_ZHANGTING", "触涨停", 14L, null),

    YANGXIAN("YANGXIAN", "阳线", 15L, null),
    YINXIAN("YINXIAN", "阴线", 16L, null),


    //插入线
    UP_INSERTION("INSERTION", "插入线", 5L, true),
    //上涨抱线
    UP_HUG("UP_HUG", "上涨抱线", 6L, true),
    //阴线+孕线
    UP_YUNXIAN("UP_YUNXIAN", "阴线+孕线", 7L, true),
    //
    YANGXIAN_GUXING("YANGXIAN_GUXING", "阳线且孤独", 17L, true),



    YINXIAN_GUXING("YINXIAN_GUXING", "阴线且孤独", 18L, false),

    //8开头 成交量相关
    STABLE_VOLUME("STABLE_VOLUME", "成交量稳定", 801L, null),

    ;
    private final String code;
    private final String description;

    private final long id;

    private Boolean isRising;

    LeafTag(String code, String description, long id, Boolean isRising) {
        this.code = code;
        this.description = description;
        this.id = id;
        this.isRising = isRising;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isRising() {
        return isRising;
    }

    public Long getId() {
        return id;
    }

    public static LeafTag fromCode(String code) {
        for (LeafTag tag : LeafTag.values()) {
            if (tag.getCode().equals(code)) {
                return tag;
            }
        }
        return null;
    }

    public static LeafTag fromId(Long id) {
        for (LeafTag tag : LeafTag.values()) {
            if (tag.getId().equals(id)) {
                return tag;
            }
        }
        return null;
    }


    public static List<LeafTag> upTags;

    public static List<LeafTag> getUpTags() {
        if (upTags != null) {
            return upTags;
        }
        upTags = new ArrayList<>();
        for (LeafTag tag : LeafTag.values()) {
            if (tag.isRising() != null && tag.isRising()) {
                upTags.add(tag);
            }
        }
        return upTags;
    }
}

