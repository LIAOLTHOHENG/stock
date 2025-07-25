package com.xzp.forum.enums;


/**
 * 股票特性标签 叶子结点枚举
 */
public enum LeafTag {


    _10CM("10CM", "10厘米", 1L),
    _20CM("20CM", "20厘米", 2L),
    CAN_DEAL("CAN_DEAL", "属于创业板和主板", 3L),
    //缩量至平稳
    STABLE("STABLE", "缩量至平稳", 4L),
    //插入线
    INSERTION("INSERTION", "插入线", 5L),
    //上涨抱线
    UP_BOUNCE("UP_BOUNCE", "上涨抱线", 6L),
    //阴线+孕线
    DOWN_BOUNCE("DOWN_BOUNCE", "阴线+孕线", 7L),
    //跌幅实体越来越小
    DOWN_ENTITY("DOWN_ENTITY", "跌幅实体越来越小", 8L),
    //涨幅大于0
    UP("UP", "涨幅大于0", 9L),
    //跌幅大于0
    DOWN("DOWN", "跌幅大于0", 10L),
    //涨幅等于0
    FLAT("FLAT", "涨幅等于0", 11L),

    ZHANGTING("ZHANGTING", "涨停", 12L),

    DIETING("DIETING", "跌停", 13L),

    TOUCH_ZHANGTING("TOUCH_ZHANGTING", "触涨停", 14L),

    YANGXIAN("YANGXIAN", "阳线", 15L),
    YINXIAN("YINXIAN", "阴线", 16L),
    //
    YANGXIAN_GUXING("YANGXIAN_GUXING", "阳线且孤独", 17L),


    ;
    private final String code;
    private final String description;

    private final long id;

    LeafTag(String code, String description, long id) {
        this.code = code;
        this.description = description;
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
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

}

