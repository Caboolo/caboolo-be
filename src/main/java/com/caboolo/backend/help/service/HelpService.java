package com.caboolo.backend.help.service;

import com.caboolo.backend.help.dto.HelpArticleDto;
import com.caboolo.backend.help.dto.HelpTopicDto;

import java.util.List;

public interface HelpService {
    List<HelpTopicDto> getAllTopics();
    List<HelpArticleDto> getArticlesByTopic(String topicId);
}
