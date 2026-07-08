package com.chronicdisease.common.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指标编码 → 单位 枚举
 */
public enum UnitEnum {

    SYSTOLIC("systolic", "mmHg"),
    DIASTOLIC("diastolic", "mmHg"),
    FASTING_GLUCOSE("fasting_glucose", "mmol/L"),
    POSTPRANDIAL_GLUCOSE("postprandial_glucose", "mmol/L"),
    HBA1C("hba1c", "%"),
    TOTAL_CHOLESTEROL("total_cholesterol", "mmol/L"),
    TRIGLYCERIDES("triglycerides", "mmol/L"),
    HDL("hdl", "mmol/L"),
    LDL("ldl", "mmol/L"),
    URIC_ACID("uric_acid", "μmol/L"),
    HEART_RATE("heart_rate", "次/分"),
    BMI("bmi", "kg/m²"),
    WEIGHT("weight", "kg"),
    WAISTLINE("waistline", "cm"),
    CREATININE("creatinine", "μmol/L"),
    ALT("alt", "U/L"),
    AST("ast", "U/L");

    private final String code;
    private final String unit;

    private static final Map<String, String> CODE_TO_UNIT =
            Arrays.stream(values()).collect(Collectors.toMap(UnitEnum::getCode, UnitEnum::getUnit));

    UnitEnum(String code, String unit) {
        this.code = code;
        this.unit = unit;
    }

    public String getCode() {
        return code;
    }

    public String getUnit() {
        return unit;
    }

    public static String getUnitByCode(String code) {
        return CODE_TO_UNIT.getOrDefault(code, "");
    }
}
