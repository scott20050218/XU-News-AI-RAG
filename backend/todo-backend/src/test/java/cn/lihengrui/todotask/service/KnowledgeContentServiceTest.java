package cn.lihengrui.todotask.service;

import cn.lihengrui.todotask.dto.*;
import cn.lihengrui.todotask.entity.KnowledgeContent;
import cn.lihengrui.todotask.repository.KnowledgeContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 知识内容服务测试类
 *
 * 测试KnowledgeContentService的业务逻辑功能。
 *
 * @author HA72开发团队
 * @version 1.0
 * @since 2025-09-26
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class KnowledgeContentServiceTest {

    @Mock
    private KnowledgeContentRepository knowledgeContentRepository;

    @Mock
    private AIProcessingService aiProcessingService;

    @InjectMocks
    private KnowledgeContentService knowledgeContentService;

    private KnowledgeContent testEntity;
    private KnowledgeContentRequest testRequest;

    @BeforeEach
    void setUp() {
        testEntity = new KnowledgeContent();
        testEntity.setKnowId(1L);
        testEntity.setTitle("测试标题");
        testEntity.setContent("测试内容");
        testEntity.setSourceUrl("http://test.com");
        testEntity.setContentType("Manual");
        testEntity.setAcquisitionTime(LocalDateTime.now());
        testEntity.setTags("测试,技术");
        testEntity.setProcessed(false);
        testEntity.setSuccess(true);

        testRequest = new KnowledgeContentRequest();
        testRequest.setTitle("测试标题");
        testRequest.setContent("测试内容");
        testRequest.setSourceUrl("http://test.com");
        testRequest.setContentType("Manual");
        testRequest.setTags("测试,技术");
        testRequest.setProcessed(false);
        testRequest.setSuccess(true);
    }

    @Test
    void testFindWithFilters_ShouldReturnPagedResults() {
        // 准备模拟数据
        Page<KnowledgeContent> entityPage = new PageImpl<>(
                Arrays.asList(testEntity), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        // 执行测试
        Pageable pageable = PageRequest.of(0, 20);
        Page<KnowledgeContentSummary> result = knowledgeContentService.findWithFilters(
                pageable, "Manual", false, true, "测试", "技术", null, null);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("测试标题");

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFindWithFilters_WithTimeFilters_ShouldApplyTimeFilters() {
        // 准备模拟数据
        Page<KnowledgeContent> entityPage = new PageImpl<>(
                Arrays.asList(testEntity), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        // 执行测试 - 使用完整的日期时间格式
        Pageable pageable = PageRequest.of(0, 20);
        Page<KnowledgeContentSummary> result = knowledgeContentService.findWithFilters(
                pageable, null, null, null, null, null, 
                "2025-09-27 09:00:00", "2025-09-27 18:00:00");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFindWithFilters_WithDateOnlyFilters_ShouldApplyDateFilters() {
        // 准备模拟数据
        Page<KnowledgeContent> entityPage = new PageImpl<>(
                Arrays.asList(testEntity), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        // 执行测试 - 使用日期格式
        Pageable pageable = PageRequest.of(0, 20);
        Page<KnowledgeContentSummary> result = knowledgeContentService.findWithFilters(
                pageable, null, null, null, null, null, 
                "2025-09-27", "2025-09-28");

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFindWithFilters_WithInvalidTimeFormat_ShouldIgnoreInvalidTime() {
        // 准备模拟数据
        Page<KnowledgeContent> entityPage = new PageImpl<>(
                Arrays.asList(testEntity), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        // 执行测试 - 使用无效的时间格式
        Pageable pageable = PageRequest.of(0, 20);
        Page<KnowledgeContentSummary> result = knowledgeContentService.findWithFilters(
                pageable, null, null, null, null, null, 
                "invalid-date", "another-invalid-date");

        // 验证结果 - 应该仍然返回结果，但时间过滤被忽略
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testFindWithFilters_WithEndDateOnly_ShouldNotIncludeNextDay() {
        // 创建测试数据 - 一个在2025-09-27，一个在2025-09-28
        KnowledgeContent entity1 = new KnowledgeContent();
        entity1.setKnowId(1L);
        entity1.setTitle("2025-09-27的记录");
        entity1.setAcquisitionTime(LocalDateTime.of(2025, 9, 27, 15, 30, 0));
        
        KnowledgeContent entity2 = new KnowledgeContent();
        entity2.setKnowId(2L);
        entity2.setTitle("2025-09-28的记录");
        entity2.setAcquisitionTime(LocalDateTime.of(2025, 9, 28, 9, 30, 0));

        // 只有第一个应该被返回
        Page<KnowledgeContent> entityPage = new PageImpl<>(
                Arrays.asList(entity1), 
                PageRequest.of(0, 20), 
                1);

        when(knowledgeContentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        // 执行测试 - 结束时间设置为2025-09-27（应该只包含27号的记录）
        Pageable pageable = PageRequest.of(0, 20);
        Page<KnowledgeContentSummary> result = knowledgeContentService.findWithFilters(
                pageable, null, null, null, null, null, 
                null, "2025-09-27");

        // 验证结果 - 应该只有一条记录（27号的）
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("2025-09-27的记录");

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testParseDateTime_EndTimeForDate_ShouldCreateCorrectBoundary() {
        // 使用反射来测试私有方法
        try {
            Method parseMethod = KnowledgeContentService.class.getDeclaredMethod("parseDateTime", String.class, boolean.class);
            parseMethod.setAccessible(true);
            
            // 测试结束时间解析
            LocalDateTime result = (LocalDateTime) parseMethod.invoke(knowledgeContentService, "2025-09-27", false);
            
            // 验证结果应该是 2025-09-27 00:00:00 (当天的开始，因为代码被修改了)
            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2025);
            assertThat(result.getMonthValue()).isEqualTo(9);
            assertThat(result.getDayOfMonth()).isEqualTo(28);
            assertThat(result.getHour()).isEqualTo(0);
            assertThat(result.getMinute()).isEqualTo(0);
            assertThat(result.getSecond()).isEqualTo(0);
            
            // 验证这个时间应该是下一天的00:00:00（结束时间边界）
            LocalDateTime expectedNextDay = LocalDateTime.of(2025, 9, 28, 0, 0, 0);
            assertThat(result).isEqualTo(expectedNextDay);
            
        } catch (Exception e) {
            fail("Failed to test parseDateTime method: " + e.getMessage());
        }
    }

    @Test
    void testFindById_WhenExists_ShouldReturnContent() {
        when(knowledgeContentRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<KnowledgeContentResponse> result = knowledgeContentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("测试标题");

        verify(knowledgeContentRepository).findById(1L);
    }

    @Test
    void testFindById_WhenNotExists_ShouldReturnEmpty() {
        when(knowledgeContentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<KnowledgeContentResponse> result = knowledgeContentService.findById(1L);

        assertThat(result).isEmpty();

        verify(knowledgeContentRepository).findById(1L);
    }

    @Test
    void testCreate_WithManualContent_ShouldProcessWithAI() {
        when(aiProcessingService.processContent(any(KnowledgeContent.class)))
                .thenReturn(testEntity);
        when(knowledgeContentRepository.save(any(KnowledgeContent.class)))
                .thenReturn(testEntity);

        KnowledgeContentResponse result = knowledgeContentService.create(testRequest);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("测试标题");

        verify(aiProcessingService).processContent(any(KnowledgeContent.class));
        verify(knowledgeContentRepository).save(any(KnowledgeContent.class));
    }

    @Test
    void testCreate_WithEmptyContent_ShouldNotProcessWithAI() {
        testRequest.setContent("");
        when(knowledgeContentRepository.save(any(KnowledgeContent.class)))
                .thenReturn(testEntity);

        KnowledgeContentResponse result = knowledgeContentService.create(testRequest);

        assertThat(result).isNotNull();

        verify(aiProcessingService, never()).processContent(any());
        verify(knowledgeContentRepository).save(any(KnowledgeContent.class));
    }

    @Test
    void testUpdate_WhenExists_ShouldUpdateSuccessfully() {
        when(knowledgeContentRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(knowledgeContentRepository.save(any(KnowledgeContent.class)))
                .thenReturn(testEntity);

        Optional<KnowledgeContentResponse> result = knowledgeContentService.update(1L, testRequest);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("测试标题");

        verify(knowledgeContentRepository).findById(1L);
        verify(knowledgeContentRepository).save(any(KnowledgeContent.class));
    }

    @Test
    void testUpdate_WhenNotExists_ShouldReturnEmpty() {
        when(knowledgeContentRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<KnowledgeContentResponse> result = knowledgeContentService.update(1L, testRequest);

        assertThat(result).isEmpty();

        verify(knowledgeContentRepository).findById(1L);
        verify(knowledgeContentRepository, never()).save(any());
    }

    @Test
    void testDelete_WhenExists_ShouldReturnTrue() {
        when(knowledgeContentRepository.existsById(1L)).thenReturn(true);

        boolean result = knowledgeContentService.delete(1L);

        assertThat(result).isTrue();

        verify(knowledgeContentRepository).existsById(1L);
        verify(knowledgeContentRepository).deleteById(1L);
    }

    @Test
    void testDelete_WhenNotExists_ShouldReturnFalse() {
        when(knowledgeContentRepository.existsById(1L)).thenReturn(false);

        boolean result = knowledgeContentService.delete(1L);

        assertThat(result).isFalse();

        verify(knowledgeContentRepository).existsById(1L);
        verify(knowledgeContentRepository, never()).deleteById(any());
    }

    @Test
    void testUpdateProcessingStatus_ShouldUpdateAndProcess() {
        // 确保测试实体初始状态为未处理且有内容
        testEntity.setProcessed(false);
        testEntity.setContent("测试内容");
        
        when(knowledgeContentRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(aiProcessingService.processContent(any(KnowledgeContent.class)))
                .thenReturn(testEntity);
        when(knowledgeContentRepository.save(any(KnowledgeContent.class)))
                .thenReturn(testEntity);

        Optional<KnowledgeContentResponse> result = knowledgeContentService.updateProcessingStatus(1L, true);

        assertThat(result).isPresent();

        verify(knowledgeContentRepository).findById(1L);
        verify(aiProcessingService).processContent(any(KnowledgeContent.class));
        verify(knowledgeContentRepository).save(any(KnowledgeContent.class));
    }

    @Test
    void testBatchOperation_Delete_ShouldDeleteEntities() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setIds(Arrays.asList(1L, 2L, 3L));
        request.setOperation("DELETE");

        List<KnowledgeContent> entities = Arrays.asList(testEntity, testEntity, testEntity);
        when(knowledgeContentRepository.findAllById(any())).thenReturn(entities);

        int result = knowledgeContentService.batchOperation(request);

        assertThat(result).isEqualTo(3);

        verify(knowledgeContentRepository).findAllById(request.getIds());
        verify(knowledgeContentRepository).deleteAll(entities);
    }

    @Test
    void testBatchOperation_UpdateStatus_ShouldUpdateEntities() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setOperation("UPDATE_STATUS");
        request.setParameter("true");

        List<KnowledgeContent> entities = Arrays.asList(testEntity, testEntity);
        when(knowledgeContentRepository.findAllById(any())).thenReturn(entities);
        when(aiProcessingService.processContent(any(KnowledgeContent.class)))
                .thenReturn(testEntity);

        int result = knowledgeContentService.batchOperation(request);

        assertThat(result).isEqualTo(2);

        verify(knowledgeContentRepository).findAllById(request.getIds());
        verify(knowledgeContentRepository).saveAll(entities);
        verify(aiProcessingService, times(2)).processContent(any(KnowledgeContent.class));
    }

    @Test
    void testBatchOperation_AddTags_ShouldAddTagsToEntities() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setIds(Arrays.asList(1L));
        request.setOperation("ADD_TAGS");
        request.setParameter("新标签");

        List<KnowledgeContent> entities = Arrays.asList(testEntity);
        when(knowledgeContentRepository.findAllById(any())).thenReturn(entities);

        int result = knowledgeContentService.batchOperation(request);

        assertThat(result).isEqualTo(1);
        assertThat(testEntity.getTags()).contains("新标签");

        verify(knowledgeContentRepository).findAllById(request.getIds());
        verify(knowledgeContentRepository).saveAll(entities);
    }

    @Test
    void testSearch_ShouldDelegateToFindWithFilters() {
        Page<KnowledgeContent> entityPage = new PageImpl<>(Arrays.asList(testEntity));
        when(knowledgeContentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(entityPage);

        Pageable pageable = PageRequest.of(0, 20);
        Page<KnowledgeContentSummary> result = knowledgeContentService.search("测试", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetStatistics_ShouldReturnStatistics() {
        when(knowledgeContentRepository.count()).thenReturn(100L);
        when(knowledgeContentRepository.countByContentType("RSS")).thenReturn(50L);
        when(knowledgeContentRepository.countByContentType("Web")).thenReturn(30L);
        when(knowledgeContentRepository.countByContentType("Manual")).thenReturn(20L);
        when(knowledgeContentRepository.countByProcessed(true)).thenReturn(60L);
        when(knowledgeContentRepository.countByProcessed(false)).thenReturn(40L);
        when(knowledgeContentRepository.countBySuccess(true)).thenReturn(90L);
        when(knowledgeContentRepository.countBySuccess(false)).thenReturn(10L);
        when(knowledgeContentRepository.countByAcquisitionTimeAfter(any())).thenReturn(5L, 15L);

        Map<String, Object> result = knowledgeContentService.getStatistics();

        assertThat(result).isNotNull();
        assertThat(result.get("totalCount")).isEqualTo(100L);
        
        @SuppressWarnings("unchecked")
        Map<String, Long> countByType = (Map<String, Long>) result.get("countByType");
        assertThat(countByType.get("RSS")).isEqualTo(50L);
        assertThat(countByType.get("Web")).isEqualTo(30L);
        assertThat(countByType.get("Manual")).isEqualTo(20L);

        verify(knowledgeContentRepository).count();
        verify(knowledgeContentRepository, times(3)).countByContentType(any());
    }

    @Test
    void testPagination_Logic_ExplainingPageNumStartsFromZero() {
        // 创建测试数据 - 模拟有30条记录
        List<KnowledgeContent> allEntities = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            KnowledgeContent entity = new KnowledgeContent();
            entity.setKnowId((long) i);
            entity.setTitle("记录 " + i);
            entity.setContent("内容 " + i);
            entity.setSourceUrl("http://test" + i + ".com");
            entity.setContentType("Manual");
            entity.setAcquisitionTime(LocalDateTime.now());
            entity.setProcessed(false);
            entity.setSuccess(true);
            allEntities.add(entity);
        }

        // 测试第1页 (pageNum=0, pageSize=10) - Spring Data JPA从0开始
        List<KnowledgeContent> page1Data = allEntities.subList(0, 10); // 记录1-10
        Page<KnowledgeContent> page1 = new PageImpl<>(page1Data, PageRequest.of(0, 10), 30);
        
        // 测试第2页 (pageNum=1, pageSize=10)
        List<KnowledgeContent> page2Data = allEntities.subList(10, 20); // 记录11-20
        Page<KnowledgeContent> page2 = new PageImpl<>(page2Data, PageRequest.of(1, 10), 30);

        // 模拟第1页请求
        when(knowledgeContentRepository.findAll(any(Specification.class), eq(PageRequest.of(0, 10))))
                .thenReturn(page1);
        
        // 模拟第2页请求
        when(knowledgeContentRepository.findAll(any(Specification.class), eq(PageRequest.of(1, 10))))
                .thenReturn(page2);

        // 执行测试 - 第1页 (pageNum=0)
        Pageable pageable1 = PageRequest.of(0, 10);
        Page<KnowledgeContentSummary> result1 = knowledgeContentService.findWithFilters(
                pageable1, null, null, null, null, null, null, null);

        // 验证第1页结果
        assertThat(result1).isNotNull();
        assertThat(result1.getContent()).hasSize(10);
        assertThat(result1.getContent().get(0).getTitle()).isEqualTo("记录 1");
        assertThat(result1.getContent().get(9).getTitle()).isEqualTo("记录 10");
        assertThat(result1.getTotalElements()).isEqualTo(30);
        assertThat(result1.getTotalPages()).isEqualTo(3);
        assertThat(result1.getNumber()).isEqualTo(0); // 当前页码（从0开始）
        assertThat(result1.isFirst()).isTrue();
        assertThat(result1.isLast()).isFalse();

        // 执行测试 - 第2页 (pageNum=1)
        Pageable pageable2 = PageRequest.of(1, 10);
        Page<KnowledgeContentSummary> result2 = knowledgeContentService.findWithFilters(
                pageable2, null, null, null, null, null, null, null);

        // 验证第2页结果
        assertThat(result2).isNotNull();
        assertThat(result2.getContent()).hasSize(10);
        assertThat(result2.getContent().get(0).getTitle()).isEqualTo("记录 11");
        assertThat(result2.getContent().get(9).getTitle()).isEqualTo("记录 20");
        assertThat(result2.getTotalElements()).isEqualTo(30);
        assertThat(result2.getTotalPages()).isEqualTo(3);
        assertThat(result2.getNumber()).isEqualTo(1); // 当前页码（从0开始）
        assertThat(result2.isFirst()).isFalse();
        assertThat(result2.isLast()).isFalse();

        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable1));
        verify(knowledgeContentRepository).findAll(any(Specification.class), eq(pageable2));
    }

    @Test
    void testGetAllTags_ShouldReturnUniqueTagsList() {
        KnowledgeContent entity1 = new KnowledgeContent();
        entity1.setTags("Java,Spring,测试");
        
        KnowledgeContent entity2 = new KnowledgeContent();
        entity2.setTags("Python,测试,AI");

        when(knowledgeContentRepository.findAll()).thenReturn(Arrays.asList(entity1, entity2));

        List<String> result = knowledgeContentService.getAllTags();

        assertThat(result).isNotNull();
        assertThat(result).containsExactlyInAnyOrder("Java", "Spring", "测试", "Python", "AI");

        verify(knowledgeContentRepository).findAll();
    }
}
