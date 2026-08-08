package com.chronicdisease.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chronicdisease.user.domain.entity.DoctorPatient;
import com.chronicdisease.user.domain.entity.DoctorProfile;
import com.chronicdisease.user.domain.vo.DoctorBriefVO;
import com.chronicdisease.user.domain.vo.UserInfoVO;

import java.util.List;

public interface IDoctorPatientService extends IService<DoctorPatient> {

    /** 患者端获取全部医生资历列表 */
    List<DoctorProfile> getDoctorList();

    /** 患者查询自己绑定的医生 */
    DoctorProfile getMyDoctor();

    /** 患者绑定医生 */
    void bindDoctor(Long doctorId);

    /** 患者解绑医生 */
    void unbindDoctor();

    /** 医生查询自己绑定的所有患者ID列表 */
    List<UserInfoVO> getMyPatients(Long doctorId);

    /** 根据患者ID查询绑定的医生信息 */
    DoctorBriefVO getByPatientId(Long patientId);
}
