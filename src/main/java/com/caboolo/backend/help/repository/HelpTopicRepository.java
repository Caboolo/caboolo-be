package com.caboolo.backend.help.repository;

import com.caboolo.backend.help.domain.HelpTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpTopicRepository extends JpaRepository<HelpTopic, Long> {
    List<HelpTopic> findAllByIsDeletedFalseOrderByDisplayOrderAsc();
}
