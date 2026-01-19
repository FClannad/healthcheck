package com.example.mapper;

import com.example.entity.AiConsultation;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI咨询Mapper接口
 */
@Mapper
public interface AiConsultationMapper {
    
    /**
     * 插入AI咨询记录
     */
    @Insert("INSERT INTO ai_consultation (user_id, user_question, ai_response, recommended_exams, session_id, status, create_time) " +
            "VALUES (#{userId}, #{userQuestion}, #{aiResponse}, #{recommendedExams}, #{sessionId}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AiConsultation consultation);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT ac.*, u.name as userName FROM ai_consultation ac " +
            "LEFT JOIN user u ON ac.user_id = u.id " +
            "WHERE ac.id = #{id}")
    AiConsultation selectById(Integer id);
    
    /**
     * 根据用户ID查询咨询历史
     */
    @Select("SELECT ac.*, u.name as userName FROM ai_consultation ac " +
            "LEFT JOIN user u ON ac.user_id = u.id " +
            "WHERE ac.user_id = #{userId} AND ac.status = 'active' " +
            "ORDER BY ac.create_time DESC")
    List<AiConsultation> selectByUserId(Integer userId);
    
    /**
     * 根据会话ID查询
     */
    @Select("SELECT ac.*, u.name as userName FROM ai_consultation ac " +
            "LEFT JOIN user u ON ac.user_id = u.id " +
            "WHERE ac.session_id = #{sessionId} AND ac.status = 'active' " +
            "ORDER BY ac.create_time ASC")
    List<AiConsultation> selectBySessionId(String sessionId);
    
    /**
     * 查询所有咨询记录（管理员用）
     */
    @Select("SELECT ac.*, u.name as userName FROM ai_consultation ac " +
            "LEFT JOIN user u ON ac.user_id = u.id " +
            "WHERE ac.status = 'active' " +
            "ORDER BY ac.create_time DESC")
    List<AiConsultation> selectAll();
    
    /**
     * 更新咨询记录
     */
    @Update("UPDATE ai_consultation SET ai_response = #{aiResponse}, " +
            "recommended_exams = #{recommendedExams}, status = #{status} " +
            "WHERE id = #{id}")
    void updateById(AiConsultation consultation);
    
    /**
     * 删除咨询记录（软删除）
     */
    @Update("UPDATE ai_consultation SET status = 'deleted' WHERE id = #{id}")
    void deleteById(Integer id);
    
    /**
     * 统计用户咨询次数
     */
    @Select("SELECT COUNT(*) FROM ai_consultation WHERE user_id = #{userId} AND status = 'active'")
    Integer countByUserId(Integer userId);
}
