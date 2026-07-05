package com.chronicdisease.common.constant;

public interface RedisConstant {

    String USER_LOGIN_KEY = "user:login:";

    String INDEX_DICT_KEY = "index:dict";

    String HOT_ARTICLE_KEY = "article:hot";

    long DEFAULT_EXPIRE_TIME = 3600L;

    long USER_TOKEN_EXPIRE_TIME = 7200L;

}
