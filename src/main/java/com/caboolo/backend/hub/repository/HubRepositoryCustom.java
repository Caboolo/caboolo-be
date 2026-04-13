package com.caboolo.backend.hub.repository;

import java.util.Collection;
import java.util.Map;

public interface HubRepositoryCustom {
    Map<Long, String> findHubIdAndNameByHubIdIn(Collection<Long> hubIds);
}
