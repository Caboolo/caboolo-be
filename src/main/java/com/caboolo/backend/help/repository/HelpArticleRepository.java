package com.caboolo.backend.help.repository;

import com.caboolo.backend.help.domain.HelpArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpArticleRepository extends JpaRepository<HelpArticle, Long> {
    List<HelpArticle> findByHelpTopicIdAndIsDeletedFalseOrderByDisplayOrderAsc(String helpTopicId);
}
