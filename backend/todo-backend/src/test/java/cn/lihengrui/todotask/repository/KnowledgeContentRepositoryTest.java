package cn.lihengrui.todotask.repository;

import cn.lihengrui.todotask.entity.KnowledgeContent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class KnowledgeContentRepositoryTest {

    @Autowired
    private KnowledgeContentRepository knowledgeContentRepository;

    @Test
    void testSaveAndFindKnowledgeContent() {
        // Create a simple test entity
        KnowledgeContent content = new KnowledgeContent();
        content.setTitle("Test Title");
        content.setContent("Test Content");
        content.setSourceUrl("http://test.com");
        content.setContentType("RSS");
        content.setAcquisitionTime(LocalDateTime.now());
        content.setTags("test");
        content.setProcessed(false);
        content.setSuccess(true);
        content.setErrorMessage(null);

        // Save the entity
        KnowledgeContent savedContent = knowledgeContentRepository.save(content);
        
        // Verify it was saved with an ID
        assertThat(savedContent.getKnowId()).isNotNull();
        assertThat(savedContent.getTitle()).isEqualTo("Test Title");

        // Test findById
        Optional<KnowledgeContent> foundContent = knowledgeContentRepository.findById(savedContent.getKnowId());
        assertThat(foundContent).isPresent();
        assertThat(foundContent.get().getTitle()).isEqualTo("Test Title");

        // Test findAll
        List<KnowledgeContent> allContent = knowledgeContentRepository.findAll();
        assertThat(allContent).hasSize(1);
        assertThat(allContent.get(0).getTitle()).isEqualTo("Test Title");

        // Test delete
        knowledgeContentRepository.deleteById(savedContent.getKnowId());
        Optional<KnowledgeContent> deletedContent = knowledgeContentRepository.findById(savedContent.getKnowId());
        assertThat(deletedContent).isNotPresent();
    }
}