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
}
