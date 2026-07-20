package com.caboolo.backend.help.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.help.dto.HelpArticleDto;
import com.caboolo.backend.help.dto.HelpTopicDto;
import com.caboolo.backend.help.service.HelpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/help")
public class HelpController extends BaseController {

    private final HelpService helpService;

    public HelpController(HelpService helpService) {
        this.helpService = helpService;
    }

    @GetMapping("/topics")
    public RestEntity<List<HelpTopicDto>> getAllTopics() {
        log.info("Received request to get all help topics");
        return successResponse(helpService.getAllTopics(), "Topics retrieved successfully");
    }

    @GetMapping("/topics/{topicId}/articles")
    public RestEntity<List<HelpArticleDto>> getArticlesByTopic(@PathVariable String topicId) {
        log.info("Received request to get articles for topicId: {}", topicId);
        return successResponse(helpService.getArticlesByTopic(topicId), "Articles retrieved successfully");
    }
}
