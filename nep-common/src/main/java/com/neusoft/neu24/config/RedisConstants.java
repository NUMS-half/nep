package com.neusoft.neu24.config;

/**
 * Redis常量
 */
public class RedisConstants {

    /**
     * 私有构造器，防止外部修改
     */
    private RedisConstants() {}

    public static final String LOGIN_SMS_KEY = "nep:user:login:sms:";

    public static final String LOGIN_TOKEN = "nep:user:login:token:";

    public static final Long LOGIN_SMS_TTL = 2L;

    public static final String CHAT_HISTORY = "nep:chat:history:";

    public static final String GRID_KEY = "nep:grid:";

    public static final String AQI_KEY = "nep:aqi:";

    public static final String PROVINCE_MAP_KEY = "nep:grid:province:";

    public static final String CITY_MAP_KEY = "nep:grid:city:";
}
