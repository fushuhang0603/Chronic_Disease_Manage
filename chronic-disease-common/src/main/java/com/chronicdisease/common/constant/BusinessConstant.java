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
    /** 已关闭 **/
    public static final Integer Remind_Status5 = 4;
    /** 已过期 **/
    public static final Integer Remind_Status6 = 5;

    /**收藏**/
    public static final Integer Collect_STATUS1 = 0;
    /**取消收藏**/
    public static final Integer Collect_STATUS2 = 1;

    /** 资讯下架 **/
    public static final Integer Article_Status_Off = 0;
    /** 资讯上架 **/
    public static final Integer Article_Status_On = 1;
    /** 初始浏览量 **/
    public static final Integer Article_View_Init = 0;

    /** 公告草稿(未发布) **/
    public static final Integer Notice_Status_Draft = 0;
    /** 公告已发布 **/
    public static final Integer Notice_Status_Published = 1;
    /** 公告已下线 **/
    public static final Integer Notice_Status_Offline = 2;

    /** 指标：正常 */
    public static final Integer INDEX_ABNORMAL_NORMAL = 0;
    /** 指标：偏高 */
    public static final Integer INDEX_ABNORMAL_HIGH = 1;
    /** 指标：偏低 */
    public static final Integer INDEX_ABNORMAL_LOW = 2;




}
