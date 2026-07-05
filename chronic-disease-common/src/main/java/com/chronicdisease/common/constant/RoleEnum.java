package com.chronicdisease.common.constant;

import lombok.Getter;

@Getter
public enum RoleEnum {
    DOCTOR("doctor","医生"),
    ADMIN("admin","超级管理员"),
    PATIENT("patient","患者");



    private final String role;
    private final String desc;
    RoleEnum(String role,String desc){
        this.role = role;
        this.desc = desc;
    }
}
