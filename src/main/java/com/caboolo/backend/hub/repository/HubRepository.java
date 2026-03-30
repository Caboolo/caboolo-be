package com.caboolo.backend.hub.repository;

import com.caboolo.backend.hub.domain.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface HubRepository extends JpaRepository<Hub, Long> {
    Optional<Hub> findByName(String name);
    Optional<Hub> findByHubId(Long hubId);
    List<Hub> findAllByHubIdIn(Collection<Long> hubIds);
    List<Hub> findAllByNameIn(List<String> names);
}
