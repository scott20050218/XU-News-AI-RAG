package cn.lihengrui.todotask.repository;

import cn.lihengrui.todotask.entity.KnowledgeContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识内容数据访问层接口
 * 
 * 该接口继承自Spring Data JPA的JpaRepository，提供对KnowledgeContent实体的
 * 标准CRUD（创建、读取、更新、删除）操作以及分页、排序等高级功能。
 * 
 * 继承的主要方法包括：
 * 1. 基本CRUD操作：
 *    - save(entity): 保存或更新实体
 *    - findById(id): 根据ID查找实体
 *    - findAll(): 查找所有实体
 *    - deleteById(id): 根据ID删除实体
 *    - count(): 统计记录总数
 * 
 * 2. 批量操作：
 *    - saveAll(entities): 批量保存实体
 *    - deleteAll(): 删除所有实体
 *    - deleteAllById(ids): 根据ID列表批量删除
 * 
 * 3. 分页和排序：
 *    - findAll(Pageable): 分页查询
 *    - findAll(Sort): 排序查询
 * 
 * 4. 存在性检查：
 *    - existsById(id): 检查指定ID的记录是否存在
 * 
 * 使用场景：
 * - RSS采集服务保存新采集的内容
 * - 网页爬取服务存储爬取的数据
 * - REST API控制器查询和管理知识内容
 * - 定时任务处理和清理过期内容
 * 
 * 注意事项：
 * - 所有方法都是事务性的，确保数据一致性
 * - 支持Spring Data JPA的方法名查询约定
 * - 可通过添加自定义方法扩展查询功能
 * 
 * 扩展建议：
 * 未来可以根据业务需求添加自定义查询方法，例如：
 * - List<KnowledgeContent> findByContentType(String contentType)
 * - List<KnowledgeContent> findByProcessedFalse()
 * - List<KnowledgeContent> findByAcquisitionTimeBetween(LocalDateTime start, LocalDateTime end)
 * - @Query自定义复杂查询
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Repository  // Spring注解：标识这是一个数据访问层组件，用于异常转换和组件扫描
public interface KnowledgeContentRepository extends JpaRepository<KnowledgeContent, Long>, JpaSpecificationExecutor<KnowledgeContent> {
    
    /**
     * 根据内容类型统计数量
     *
     * @param contentType 内容类型
     * @return 数量
     */
    long countByContentType(String contentType);

    /**
     * 根据处理状态统计数量
     *
     * @param processed 处理状态
     * @return 数量
     */
    long countByProcessed(boolean processed);

    /**
     * 根据成功状态统计数量
     *
     * @param success 成功状态
     * @return 数量
     */
    long countBySuccess(boolean success);

    /**
     * 统计指定时间之后的记录数量
     *
     * @param dateTime 时间点
     * @return 数量
     */
    long countByAcquisitionTimeAfter(LocalDateTime dateTime);

    /**
     * 根据时间范围查询内容
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 内容列表
     */
    List<KnowledgeContent> findByAcquisitionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取内容类型分布统计
     *
     * @return 内容类型和数量的数组列表
     */
    @Query("SELECT kc.contentType, COUNT(kc) FROM KnowledgeContent kc GROUP BY kc.contentType")
    List<Object[]> findContentTypeDistribution();
    
    /*
     * 可扩展的自定义查询方法示例：
     * 
     * // 根据内容类型查询
     * List<KnowledgeContent> findByContentType(String contentType);
     * 
     * // 查询未处理的内容
     * List<KnowledgeContent> findByProcessedFalse();
     * 
     * // 根据来源URL查询（用于去重）
     * Optional<KnowledgeContent> findBySourceUrl(String sourceUrl);
     * 
     * // 按采集时间范围查询
     * List<KnowledgeContent> findByAcquisitionTimeBetween(
     *     LocalDateTime startTime, LocalDateTime endTime);
     * 
     * // 查询成功采集的内容并按时间降序排列
     * List<KnowledgeContent> findBySuccessTrueOrderByAcquisitionTimeDesc();
     * 
     * // 根据标签模糊查询
     * @Query("SELECT kc FROM KnowledgeContent kc WHERE kc.tags LIKE %:tag%")
     * List<KnowledgeContent> findByTagsContaining(@Param("tag") String tag);
     * 
     * // 统计不同内容类型的数量
     * @Query("SELECT kc.contentType, COUNT(kc) FROM KnowledgeContent kc GROUP BY kc.contentType")
     * List<Object[]> countByContentType();
     */
}
