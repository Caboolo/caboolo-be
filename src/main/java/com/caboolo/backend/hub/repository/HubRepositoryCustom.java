package com.caboolo.backend.hub.repository;

import java.util.Collection;
import java.util.Map;

public interface HubRepositoryCustom {
    Map<String, String> findHubIdAndNameByHubIdIn(Collection<String> hubIds);
}
