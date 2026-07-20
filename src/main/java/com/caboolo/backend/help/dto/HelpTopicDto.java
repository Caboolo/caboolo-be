package com.caboolo.backend.help.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HelpTopicDto {
    private String helpTopicId;
    private String title;
    private String description;
    private int displayOrder;
}
