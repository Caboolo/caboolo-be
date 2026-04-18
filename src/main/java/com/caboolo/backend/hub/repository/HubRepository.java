package com.caboolo.backend.hub.repository;

import com.caboolo.backend.hub.domain.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HubRepository extends JpaRepository<Hub, Long>, HubRepositoryCustom {
    List<Hub> findByHubIdIn(Collection<String> hubIds);
}
