package com.caboolo.backend.help.service;

import com.caboolo.backend.help.domain.HelpArticle;
import com.caboolo.backend.help.domain.HelpTopic;
import com.caboolo.backend.help.dto.HelpArticleDto;
import com.caboolo.backend.help.dto.HelpTopicDto;
import com.caboolo.backend.help.repository.HelpArticleRepository;
import com.caboolo.backend.help.repository.HelpTopicRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HelpServiceImpl implements HelpService {

    private final HelpTopicRepository helpTopicRepository;
    private final HelpArticleRepository helpArticleRepository;

    public HelpServiceImpl(HelpTopicRepository helpTopicRepository, HelpArticleRepository helpArticleRepository) {
        this.helpTopicRepository = helpTopicRepository;
        this.helpArticleRepository = helpArticleRepository;
    }

    @Override
    public List<HelpTopicDto> getAllTopics() {
        log.info("Fetching all help topics");
        List<HelpTopic> topics = helpTopicRepository.findAllByIsDeletedFalseOrderByDisplayOrderAsc();
        return topics.stream()
                .map(topic -> HelpTopicDto.builder()
                        .helpTopicId(topic.getHelpTopicId())
                        .title(topic.getTitle())
                        .description(topic.getDescription())
                        .displayOrder(topic.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<HelpArticleDto> getArticlesByTopic(String topicId) {
        log.info("Fetching help articles for topicId: {}", topicId);
        List<HelpArticle> articles = helpArticleRepository.findByHelpTopicIdAndIsDeletedFalseOrderByDisplayOrderAsc(topicId);
        return articles.stream()
                .map(article -> HelpArticleDto.builder()
                        .helpArticleId(article.getHelpArticleId())
                        .helpTopicId(article.getHelpTopicId())
                        .question(article.getQuestion())
                        .answer(article.getAnswer())
                        .displayOrder(article.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }
}
