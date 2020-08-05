package com.imooc.service.search;

/**
 * 索引关键词统一定义 todo 操作时要用到的常量 （注意下，不是很懂）
 * 为什么既要用es又要用MySQL？
 * 因为es做检索，MySQL做原始数据的存储，MySQL支持事务
 * Created by 瓦力.
 */
public class HouseIndexKey {
    public static final String HOUSE_ID = "houseId";

    public static final String TITLE = "title";

    public static final String PRICE = "price";
    public static final String AREA = "area";
    public static final String CREATE_TIME = "createTime";
    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    public static final String CITY_EN_NAME = "cityEnName";
    public static final String REGION_EN_NAME = "regionEnName";
    public static final String DIRECTION = "direction";
    public static final String DISTANCE_TO_SUBWAY = "distanceToSubway";
    public static final String STREET = "street";
    public static final String DISTRICT = "district";
    public static final String DESCRIPTION = "description";
    public static final String LAYOUT_DESC = "layoutDesc";
    public static final String TRAFFIC = "traffic";
    public static final String ROUND_SERVICE = "roundService";
    public static final String RENT_WAY = "rentWay";
    public static final String SUBWAY_LINE_NAME = "subwayLineName";
    public static final String SUBWAY_STATION_NAME = "subwayStationName";
    public static final String TAGS = "tags";

    public static final String AGG_DISTRICT = "agg_district";
    public static final String AGG_REGION = "agg_region";
}
