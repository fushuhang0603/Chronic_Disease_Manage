package com.chronicdisease.common.constant;

import io.swagger.v3.oas.models.security.SecurityScheme;

public class BusinessConstant {

    public static final Integer isDelete = 1;
    public static final Integer isNotDelete = 0;

    /** 不重复 **/
    public static final String Repeat_Type1 = "none";
    /** 每天 **/
    public static final String Repeat_Type2 = "daily";
    /** 每周 **/
    public static final String Repeat_Type3 = "weekly";
    /** 每月 **/
    public static final String Repeat_Type4 = "monthly";

    /** 待提醒 **/
    public static final Integer Remind_Status1 = 0;
    /** 已推送 **/
    public static final Integer Remind_Status2 = 1;
    /** 已读 **/
    public static final Integer Remind_Status3 = 2;
    /** 已完成 **/
    public static final Integer Remind_Status4 = 3;


}
