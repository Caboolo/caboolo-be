package com.caboolo.backend.help.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "help_article")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HelpArticle extends GenericIdEntity {

    @Column(name = "help_article_id", nullable = false, unique = true)
    private String helpArticleId;

    @Column(name = "help_topic_id", nullable = false)
    private String helpTopicId;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
