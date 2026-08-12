package com.assistant.chronicdiseaseagent.common.Entity;

import lombok.Data;

@Data
public class DoctorProfileQuery {

    private String doctorName;
    private String hospital;
    private String department;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
