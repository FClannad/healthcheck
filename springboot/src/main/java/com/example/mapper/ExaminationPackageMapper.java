package com.example.mapper;

import com.example.entity.ExaminationPackage;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ExaminationPackageMapper {

    int insert(ExaminationPackage examinationPackage);

    void updateById(ExaminationPackage examinationPackage);

    void deleteById(Integer id);

    @Select("select * from `examination_package` where id = #{id}")
    ExaminationPackage selectById(Integer id);

    List<ExaminationPackage> selectAll(ExaminationPackage examinationPackage);

    /**
     * 获取热门套餐（按预约数量排序）
     */
    @Select("select ep.* from `examination_package` ep " +
            "left join (select examination_id, count(*) as order_count from `examination_order` " +
            "where order_type = '套餐体检' and create_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "group by examination_id) oc on ep.id = oc.examination_id " +
            "order by IFNULL(oc.order_count, 0) desc limit #{limit}")
    List<ExaminationPackage> getHotPackages(int limit);

}
