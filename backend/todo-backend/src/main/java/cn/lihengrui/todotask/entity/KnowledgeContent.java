package cn.lihengrui.todotask.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识内容实体类
 * 
 * 该实体类用于存储从各种来源（RSS源、网页等）采集到的知识内容信息。
 * 包含内容的基本信息、元数据以及处理状态等字段。
 * 
 * 数据库表映射：knowledge_content
 * 
 * 主要用途：
 * 1. 存储RSS源采集的文章内容
 * 2. 存储网页爬取的内容信息
 * 3. 记录内容的来源和采集时间
 * 4. 跟踪内容的处理状态（是否经过AI智能代理处理）
 * 5. 记录采集过程中的成功/失败状态
 * 
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@Data                    // Lombok注解：自动生成getter、setter、equals、hashCode、toString方法
@NoArgsConstructor       // Lombok注解：生成无参构造函数
@AllArgsConstructor      // Lombok注解：生成全参构造函数
@Entity                  // JPA注解：标识这是一个JPA实体类
@Table(name = "knowledge_content")  // JPA注解：指定数据库表名
public class KnowledgeContent {

    /**
     * 知识内容主键ID
     * 
     * 数据库自增长主键，唯一标识每条知识内容记录
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 使用数据库自增策略
    @Column(name = "know_id")  // 映射到数据库的know_id列
    private Long knowId;

    /**
     * 内容标题
     * 
     * 存储文章、网页的标题信息，不能为空
     * 对于RSS源：取自feed item的title
     * 对于网页：取自页面的<title>标签
     */
    @Column(nullable = false)
    private String title;

    /**
     * 内容正文
     * 
     * 存储文章或网页的主要文本内容，最大长度50000字符
     * 对于RSS源：取自feed item的description
     * 对于网页：取自页面解析后的文本内容
     * 
     * 注意：如果内容超过50000字符会导致数据截断错误，
     * 建议在保存前进行长度检查或内容摘要处理
     */
    @Column(length = 50000)
    private String content;

    /**
     * 来源URL
     * 
     * 记录内容的原始来源地址，不能为空
     * 用于：
     * 1. 内容溯源和版权标识
     * 2. 内容去重判断
     * 3. 后续内容更新检查
     */
    @Column(nullable = false)
    private String sourceUrl;

    /**
     * 内容类型
     * 
     * 标识内容的来源类型，不能为空
     * 可选值：
     * - "RSS"：来自RSS源的内容
     * - "Web"：来自网页爬取的内容
     * - 可扩展：未来可添加"API"、"Document"等类型
     */
    @Column(nullable = false)
    private String contentType;

    /**
     * 采集时间
     * 
     * 记录内容被系统采集的时间戳，不能为空
     * 用于：
     * 1. 内容时效性判断
     * 2. 采集历史追踪
     * 3. 数据清理和归档
     */
    @Column(nullable = false)
    private LocalDateTime acquisitionTime;

    /**
     * 标签信息
     * 
     * 以逗号分隔的标签字符串，可为空
     * 对于RSS源：从feed item的categories提取
     * 对于网页：可通过关键词提取或手动标注
     * 
     * 示例：
     * - "技术,Java,Spring Boot"
     * - "新闻,科技,人工智能"
     */
    private String tags;

    /**
     * 处理状态标识
     * 
     * 标识内容是否已经过智能代理的深度处理，不能为空
     * - true：已处理（可能包括内容分析、摘要生成、关键词提取等）
     * - false：未处理（原始采集状态）
     * 
     * 该字段为未来AI智能处理功能预留
     */
    @Column(nullable = false)
    private boolean processed;

    /**
     * 采集成功状态
     * 
     * 标识本次内容采集是否成功，不能为空
     * - true：采集成功，内容完整可用
     * - false：采集失败，可能存在网络错误、解析错误等问题
     * 
     * 配合errorMessage字段使用，便于采集问题的诊断和统计
     */
    @Column(nullable = false)
    private boolean success;

    /**
     * 错误信息
     * 
     * 当采集失败时记录具体的错误信息，可为空
     * 仅在success=false时有值，用于：
     * 1. 问题排查和调试
     * 2. 采集质量监控
     * 3. 自动化错误处理
     * 
     * 示例错误信息：
     * - "网络连接超时"
     * - "RSS解析失败：格式不正确"
     * - "数据长度超出限制"
     */
    private String errorMessage;
}
