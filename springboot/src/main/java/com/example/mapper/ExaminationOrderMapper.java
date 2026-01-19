package com.example.mapper;

import com.example.entity.ExaminationOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExaminationOrderMapper {

    int insert(ExaminationOrder examinationOrder);

    void updateById(ExaminationOrder examinationOrder);

    void deleteById(Integer id);

    @Select("select * from `examination_order` where id = #{id}")
    ExaminationOrder selectById(Integer id);

    List<ExaminationOrder> selectAll(ExaminationOrder examinationOrder);

    @Select("select * from `examination_order` where reserve_date = #{reserveDate} " +
            "and examination_id = #{examinationId} " +
            "and order_type = #{orderType} " +
            "and user_id = #{userId} " +
            "and status <> '已取消' and status <> '已完成'")
    ExaminationOrder selectByExaminationIdAndOrderType(@Param("reserveDate") String reserveDate,
                                                       @Param("examinationId") Integer examinationId,
                                                       @Param("orderType") String orderType,
                                                       @Param("userId") Integer userId);
    @Select("select * from `examination_order` where doctor_id = #{doctorId} and status = '待检查'")
    List<ExaminationOrder> selectPrepareOrders(Integer doctorId);

    // 缓存预热相关查询方法
    
    /**
     * 查询最近N天的订单（用于缓存预热）
     */
    @Select("select * from `examination_order` where create_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) order by create_time desc limit 100")
    List<ExaminationOrder> selectRecentOrders(@Param("days") int days);

    /**
     * 查询待处理的订单（医生端常查询）
     */
    @Select("select * from `examination_order` where status in ('待审批', '已审批', '待上传报告') order by create_time desc limit 50")
    List<ExaminationOrder> selectPendingOrders();

    /**
     * 查询明天有预约的订单
     */
    @Select("select * from `examination_order` where reserve_date = DATE_FORMAT(DATE_ADD(NOW(), INTERVAL 1 DAY), '%Y-%m-%d')")
    List<ExaminationOrder> selectTomorrowOrders();

    /**
     * 获取今日订单数量
     */
    @Select("select count(*) from `examination_order` where DATE(create_time) = CURDATE()")
    Integer getTodayOrderCount();

    /**
     * 获取本月订单数量
     */
    @Select("select count(*) from `examination_order` where DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(NOW(), '%Y-%m')")
    Integer getMonthOrderCount();

    /**
     * 按状态统计订单数量
     */
    @Select("select count(*) from `examination_order` where status = #{status}")
    Integer getOrderCountByStatus(@Param("status") String status);

    /**
     * 获取医生工作负载统计
     */
    @Select("select doctor_id, count(*) as order_count from `examination_order` where status in ('待审批', '已审批', '待上传报告') group by doctor_id")
    List<Object[]> getDoctorWorkloadStats();

    /**
     * 查询某医生某天的已预约时间段
     * @param doctorId 医生ID
     * @param reserveDate 预约日期
     * @return 已预约的订单列表
     */
    @Select("select * from `examination_order` where doctor_id = #{doctorId} and reserve_date = #{reserveDate} and status not in ('已取消', '审批拒绝')")
    List<ExaminationOrder> selectBookedTimeSlots(@Param("doctorId") Integer doctorId, @Param("reserveDate") String reserveDate);
}
