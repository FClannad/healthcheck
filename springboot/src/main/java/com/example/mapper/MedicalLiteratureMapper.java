package com.example.mapper;

import com.example.entity.MedicalLiterature;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 医疗文献数据访问层
 */
public interface MedicalLiteratureMapper {

    /**
     * 插入医疗文献
     */
    int insert(MedicalLiterature literature);

    /**
     * 根据ID查询
     */
    @Select("select * from `medical_literature` where id = #{id}")
    MedicalLiterature selectById(Integer id);

    /**
     * 根据标题查询（防止重复）
     */
    @Select("select * from `medical_literature` where title = #{title}")
    MedicalLiterature selectByTitle(String title);

    /**
     * 更新文献信息
     */
    void updateById(MedicalLiterature literature);

    /**
     * 删除文献（物理删除）
     */
    @Delete("delete from `medical_literature` where id = #{id}")
    void deleteById(Integer id);

    /**
     * 查询所有文献（支持条件查询）
     */
    @Select("<script>" +
            "SELECT id, title, authors, journal, publish_date, abstract_content, keywords, source_url, crawl_source, status, create_time " +
            "FROM medical_literature " +
            "<where>" +
            "<if test='title != null and title != \"\"'>" +
            "AND title LIKE CONCAT('%', #{title}, '%')" +
            "</if>" +
            "<if test='authors != null and authors != \"\"'>" +
            "AND authors LIKE CONCAT('%', #{authors}, '%')" +
            "</if>" +
            "<if test='journal != null and journal != \"\"'>" +
            "AND journal LIKE CONCAT('%', #{journal}, '%')" +
            "</if>" +
            "<if test='keywords != null and keywords != \"\"'>" +
            "AND keywords LIKE CONCAT('%', #{keywords}, '%')" +
            "</if>" +
            "<if test='crawlSource != null and crawlSource != \"\"'>" +
            "AND crawl_source = #{crawlSource}" +
            "</if>" +
            "<if test='status != null and status != \"\"'>" +
            "AND status = #{status}" +
            "</if>" +
            "</where>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<MedicalLiterature> selectAll(MedicalLiterature literature);

    /**
     * 高级搜索文献
     */
    List<MedicalLiterature> searchLiterature(@Param("keyword") String keyword,
                                           @Param("category") String category,
                                           @Param("language") String language,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    /**
     * 获取最新文献
     */
    @Select("select * from `medical_literature` order by create_time desc limit #{limit}")
    List<MedicalLiterature> getLatestLiterature(int limit);

    /**
     * 获取推荐文献（基于关键词匹配）
     */
    @Select("select * from `medical_literature` where " +
            "(keywords like CONCAT('%', #{keyword}, '%') or " +
            "title like CONCAT('%', #{keyword}, '%') or " +
            "abstract_content like CONCAT('%', #{keyword}, '%')) " +
            "order by create_time desc limit #{limit}")
    List<MedicalLiterature> getRecommendedLiterature(@Param("keyword") String keyword, @Param("limit") int limit);

    /**
     * 批量插入文献
     */
    int batchInsert(@Param("literatures") List<MedicalLiterature> literatures);

    /**
     * 获取来源统计
     */
    @Select("SELECT crawl_source as category, COUNT(*) as count FROM medical_literature WHERE crawl_source IS NOT NULL AND crawl_source != '' GROUP BY crawl_source ORDER BY count DESC")
    List<Map<String, Object>> getCategoryStatistics();

    /**
     * 更新文献状态
     */
    @Update("UPDATE medical_literature SET status = #{status} WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") String status);

    /**
     * 统计文献总数
     */
    @Select("SELECT COUNT(*) FROM medical_literature")
    int count();

    /**
     * 根据状态统计文献数量
     */
    @Select("SELECT COUNT(*) FROM medical_literature WHERE status = #{status}")
    int countByStatus(@Param("status") String status);

    /**
     * 统计状态为NULL的文献数量
     */
    @Select("SELECT COUNT(*) FROM medical_literature WHERE status IS NULL OR status = ''")
    int countByNullStatus();

    /**
     * 将NULL或空状态更新为active
     */
    @Update("UPDATE medical_literature SET status = 'active' WHERE status IS NULL OR status = ''")
    int updateNullStatusToActive();

    /**
     * 检查是否存在相同标题和作者的文献（用于去重）
     */
    @Select("SELECT COUNT(*) > 0 FROM medical_literature WHERE title = #{title} AND authors = #{authors}")
    boolean existsByTitleAndAuthors(@Param("title") String title, @Param("authors") String authors);

    /**
     * 统计总文献数量（支持条件）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM medical_literature " +
            "<where>" +
            "<if test='condition != null'>" +
            "AND ${condition}" +
            "</if>" +
            "</where>" +
            "</script>")
    int selectCount(@Param("condition") String condition);

    /**
     * 按来源统计文献数量
     */
    @Select("SELECT COUNT(*) FROM medical_literature WHERE crawl_source = #{source}")
    int countBySource(@Param("source") String source);

    /**
     * 获取最近的文献
     */
    @Select("SELECT id, title, authors, journal, publish_date, abstract_content, keywords, source_url, crawl_source, create_time " +
            "FROM medical_literature ORDER BY create_time DESC LIMIT #{limit}")
    List<MedicalLiterature> selectRecentLiteratures(@Param("limit") int limit);

    /**
     * 统计今日爬取的文献数量
     */
    @Select("SELECT COUNT(*) FROM medical_literature WHERE DATE(create_time) = CURDATE()")
    int countTodayLiteratures();

    /**
     * 按日期统计文献数量
     */
    @Select("SELECT DATE(create_time) as date, COUNT(*) as count " +
            "FROM medical_literature " +
            "WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(create_time) " +
            "ORDER BY date DESC")
    List<Map<String, Object>> countByDateRange(@Param("days") int days);

    /**
     * 按来源统计文献数量（详细）
     */
    @Select("SELECT crawl_source as source, COUNT(*) as count " +
            "FROM medical_literature " +
            "GROUP BY crawl_source " +
            "ORDER BY count DESC")
    List<Map<String, Object>> getSourceStatistics();
}