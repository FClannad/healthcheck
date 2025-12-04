package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Doctor;
import com.example.exception.CustomException;
import com.example.mapper.DoctorMapper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * 医生业务层
 */
@Service
public class DoctorService extends BaseAccountService {

    @Resource
    private DoctorMapper doctorMapper;

    @Override
    protected RoleEnum getRoleEnum() {
        return RoleEnum.DOCTOR;
    }

    public void add(Doctor doctor) {
        Doctor dbDoctor = doctorMapper.selectByUsername(doctor.getUsername());
        if (ObjectUtil.isNotNull(dbDoctor)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(doctor.getPassword())) {
            doctor.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        if (ObjectUtil.isEmpty(doctor.getName())) {
            doctor.setName(doctor.getUsername());
        }
        doctor.setRole(RoleEnum.DOCTOR.name());
        doctorMapper.insert(doctor);
    }

    public void updateById(Doctor doctor) {
        doctorMapper.updateById(doctor);
    }

    public void deleteById(Integer id) {
        doctorMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            doctorMapper.deleteById(id);
        }
    }

    public Doctor selectById(Integer id) {
        return doctorMapper.selectById(id);
    }

    public List<Doctor> selectAll(Doctor doctor) {
        return doctorMapper.selectAll(doctor);
    }

    public PageInfo<Doctor> selectPage(Doctor doctor, Integer pageNum, Integer pageSize) {
        return selectPage(pageNum, pageSize, doctor.getName(), doctorMapper.selectAll(doctor));
    }

    /**
     * 登录
     */
    public Doctor login(@RequestBody Account account) {
        Doctor dbDoctor = doctorMapper.selectByUsername(account.getUsername());
        return (Doctor) super.login(account, dbDoctor);
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Doctor dbDoctor = doctorMapper.selectByUsername(account.getUsername());
        super.updatePassword(account, dbDoctor);
        doctorMapper.updateById(dbDoctor);
    }

    /**
     * 根据职称ID查询医生数量
     */
    public Integer selectByTitleId(Integer titleId) {
        return doctorMapper.selectByTitleId(titleId);
    }

    /**
     * 根据科室ID查询医生数量
     */
    public Integer selectByOfficeId(Integer officeId) {
        return doctorMapper.selectByOfficeId(officeId);
    }
}