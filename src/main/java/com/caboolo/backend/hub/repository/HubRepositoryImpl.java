package com.caboolo.backend.hub.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class HubRepositoryImpl implements HubRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> findHubIdAndNameByHubIdIn(Collection<String> hubIds) {
        if (hubIds == null || hubIds.isEmpty()) {
            return new HashMap<>();
        }

        String sql = "SELECT hub_id, name FROM hub WHERE hub_id IN (:hubIds)";
        List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("hubIds", hubIds)
                .getResultList();

        Map<String, String> map = new HashMap<>();
        for (Object[] row : results) {
            String id = String.valueOf(row[0]);
            String name = (String) row[1];
            map.put(id, name);
        }
        return map;
    }
}
