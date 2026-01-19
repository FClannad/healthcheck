package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.ExaminationOrder;
import com.example.entity.ExaminationPackage;
import com.example.entity.PhysicalExamination;
import com.example.exception.CustomException;
import com.example.mapper.ExaminationOrderMapper;
import com.example.utils.TokenUtils;
import com.example.utils.RedisUtils;
import com.example.utils.RedisDistributedLock;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 业务层方法
 */
@Service
public class ExaminationOrderService {

    private static final Logger logger = LoggerFactory.getLogger(ExaminationOrderService.class);

    @Resource
    private ExaminationOrderMapper examinationOrderMapper;

    @Resource
    private PhysicalExaminationService physicalExaminationService;

    @Resource
    private ExaminationPackageService examinationPackageService;

    @Autowired
    private RedisUtils redisUtils;

    public void add(ExaminationOrder examinationOrder) {
        examinationOrder.setCreateTime(DateUtil.now());
        Account currentUser = TokenUtils.getCurrentUser();
        examinationOrder.setUserId(currentUser.getId());
        // 预约的项目
        Integer examinationId = examinationOrder.getExaminationId();
        if ("普通体检".equals(examinationOrder.getOrderType())) {
            // 判断当前是否存在相同项目待审批的订单
            ExaminationOrder order = examinationOrderMapper.selectByExaminationIdAndOrderType(examinationOrder.getReserveDate(), examinationId, "普通体检", currentUser.getId());
            if (order != null) {
                throw new CustomException("500", "您已经预约过该项目" + order.getReserveDate() + "的检查，请不要重复预约");
            }
            PhysicalExamination physicalExamination = physicalExaminationService.selectById(examinationId);
            examinationOrder.setMoney(physicalExamination.getMoney());
            examinationOrder.setDoctorId(physicalExamination.getDoctorId());
        } else {
            // 套餐体检
            // 判断当前是否存在相同项目待审批的订单
            ExaminationOrder order = examinationOrderMapper.selectByExaminationIdAndOrderType(examinationOrder.getReserveDate(), examinationId, "套餐体检", currentUser.getId());
            if (order != null) {
                throw new CustomException("500", "您已经预约过该项目" + order.getReserveDate() + "的检查，请不要重复预约");
            }
            ExaminationPackage examinationPackage = examinationPackageService.selectById(examinationId);
            examinationOrder.setMoney(examinationPackage.getMoney());
            examinationOrder.setDoctorId(examinationPackage.getDoctorId());
        }
        Date date = new Date();
        String orderNo = DateUtil.format(date, "yyyyMMdd") + date.getTime();  // 唯一的订单号
        examinationOrder.setOrderNo(orderNo);
        examinationOrder.setStatus("待审批");
        examinationOrderMapper.insert(examinationOrder);
    }

    @CachePut(value = "examinationOrders", key = "#examinationOrder.id")
    @CacheEvict(value = "examinationOrdersPage", allEntries = true)
    public void updateById(ExaminationOrder examinationOrder) {
        examinationOrderMapper.updateById(examinationOrder);
    }

    @CacheEvict(value = {"examinationOrders", "examinationOrdersPage"}, key = "#id", allEntries = true)
    public void deleteById(Integer id) {
        examinationOrderMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            examinationOrderMapper.deleteById(id);
        }
    }

    @Cacheable(value = "examinationOrders", key = "#id")
    public ExaminationOrder selectById(Integer id) {
        return examinationOrderMapper.selectById(id);
    }

    public List<ExaminationOrder> selectAll(ExaminationOrder examinationOrder) {
        return examinationOrderMapper.selectAll(examinationOrder);
    }

    public PageInfo<ExaminationOrder> selectPage(ExaminationOrder examinationOrder, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())) {
            examinationOrder.setUserId(currentUser.getId());
        }
        if (RoleEnum.DOCTOR.name().equals(currentUser.getRole())) {
            examinationOrder.setDoctorId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<ExaminationOrder> list = examinationOrderMapper.selectAll(examinationOrder);
        logger.info("Found {} examination orders for page {}", list.size(), pageNum);
        
        for (ExaminationOrder order : list) {
            List<PhysicalExamination> examinationList = new ArrayList<>();
            if (order.getOrderType().equals("套餐体检")) {
                Integer examinationId = order.getExaminationId();  // 套餐体检项目ID
                // 查询套餐体检  包含的普通体检项目列表
                try {
                    ExaminationPackage examinationPackage = examinationPackageService.selectById(examinationId);
                    if (examinationPackage != null && examinationPackage.getExaminations() != null) {
                        logger.debug("Processing package examination: {} for order: {}", examinationPackage.getName(), order.getOrderNo());
                        JSONArray examinationIds = JSONUtil.parseArray(examinationPackage.getExaminations());
                        for (Object physicalExaminationId : examinationIds) {
                            PhysicalExamination physicalExamination = physicalExaminationService.selectById((Integer) physicalExaminationId);// 查询普通体检的信息
                            if (physicalExamination != null) {
                                examinationList.add(physicalExamination);
                            }
                        }
                    } else {
                        logger.warn("Examination package is null or has no examinations for ID: {}", examinationId);
                    }
                } catch (Exception e) {
                    logger.error("Error processing examination package for order: {}, packageId: {}", order.getOrderNo(), examinationId, e);
                }
            }
            // 再设置到 order里面
            order.setExaminationList(examinationList);
        }
        return PageInfo.of(list);
    }


    public List<ExaminationOrder> selectScheduleData() {
        Account currentUser = TokenUtils.getCurrentUser();
        return examinationOrderMapper.selectPrepareOrders(currentUser.getId());
    }

    /**
     * 获取某医生某天的所有时间段及其预约状态
     * @param doctorId 医生ID
     * @param reserveDate 预约日期
     * @return 时间段列表，包含是否已被预约的状态
     */
    public List<java.util.Map<String, Object>> getTimeSlots(Integer doctorId, String reserveDate) {
        // 定义工作时间段：08:00 - 18:00，每个时间段45分钟
        List<java.util.Map<String, Object>> timeSlots = new ArrayList<>();
        
        // 查询该医生该天已预约的时间段
        List<ExaminationOrder> bookedOrders = examinationOrderMapper.selectBookedTimeSlots(doctorId, reserveDate);
        
        // 生成所有时间段 (08:00-18:00, 每45分钟一个时间段)
        String[] startTimes = {"08:00", "08:45", "09:30", "10:15", "11:00", "11:45",
                               "13:00", "13:45", "14:30", "15:15", "16:00", "16:45", "17:30"};
        String[] endTimes = {"08:45", "09:30", "10:15", "11:00", "11:45", "12:30",
                             "13:45", "14:30", "15:15", "16:00", "16:45", "17:30", "18:15"};
        
        for (int i = 0; i < startTimes.length; i++) {
            java.util.Map<String, Object> slot = new java.util.HashMap<>();
            slot.put("startTime", startTimes[i]);
            slot.put("endTime", endTimes[i]);
            slot.put("label", startTimes[i] + " - " + endTimes[i]);
            
            // 检查该时间段是否已被预约
            boolean isBooked = false;
            String bookedUserName = null;
            for (ExaminationOrder order : bookedOrders) {
                if (startTimes[i].equals(order.getStartTime())) {
                    isBooked = true;
                    bookedUserName = order.getUserName();
                    break;
                }
            }
            slot.put("booked", isBooked);
            slot.put("bookedUserName", bookedUserName);
            
            timeSlots.add(slot);
        }
        
        return timeSlots;
    }
}
