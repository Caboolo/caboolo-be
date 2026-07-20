package com.caboolo.backend.help.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HelpArticleDto {
    private String helpArticleId;
    private String helpTopicId;
    private String question;
    private String answer;
    private int displayOrder;
}
