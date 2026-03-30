package com.caboolo.backend.hub.service;

import com.caboolo.backend.core.idgen.SequenceGenerator;
import com.caboolo.backend.hub.domain.Hub;
import com.caboolo.backend.hub.dto.HubDto;
import com.caboolo.backend.hub.repository.HubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HubService {

    private static final String REDIS_HUB_KEY = "hubs:geo";
    private final HubRepository hubRepository;
    private final GeoOperations<String, String> geoOps;
    private final SequenceGenerator sequenceGenerator;

    public HubService(HubRepository hubRepository, GeoOperations<String, String> geoOps, SequenceGenerator sequenceGenerator) {
        this.hubRepository = hubRepository;
        this.geoOps = geoOps;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Transactional
    public void bulkStoreHubs(List<HubDto> hubDtos) {
        List<Hub> hubs = hubDtos.stream()
                .map(dto -> Hub.Builder.hub()
                        .withHubId(sequenceGenerator.nextId())
                        .withName(dto.getName())
                        .withType(dto.getType())
                        .withCity(dto.getCity())
                        .withLatitude(dto.getLatitude())
                        .withLongitude(dto.getLongitude())
                        .build())
                .collect(Collectors.toList());

        // 1. Store in MySQL
        hubRepository.saveAll(hubs);

        // 2. Store in Redis GEO
        List<RedisGeoCommands.GeoLocation<String>> locations = hubs.stream()
                .map(hub -> new RedisGeoCommands.GeoLocation<>(
                        hub.getName(),
                        new Point(hub.getLongitude(), hub.getLatitude())
                ))
                .collect(Collectors.toList());

        geoOps.add(REDIS_HUB_KEY, locations);
        log.info("Bulk stored {} hubs in Redis and MySQL", hubs.size());
    }

    public List<HubDto> findNearestHubs(double longitude, double latitude, double radiusKm) {
        Point center = new Point(longitude, latitude);
        Distance radius = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(REDIS_HUB_KEY, center.toString(), radius, args);

        if (results == null || results.getContent().isEmpty()) {
            return new ArrayList<>();
        }

        List<String> nearestNames = results.getContent().stream()
                .map(res -> res.getContent().getName())
                .collect(Collectors.toList());

        // Fetch details from MySQL to get type and city
        Map<String, Hub> hubMap = hubRepository.findAllByNameIn(nearestNames).stream()
                .collect(Collectors.toMap(Hub::getName, h -> h));

        List<HubDto> nearestHubs = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            String name = result.getContent().getName();
            Hub hub = hubMap.get(name);

            if (hub != null) {
                nearestHubs.add(HubDto.builder()
                        .name(name)
                        .type(hub.getType())
                        .city(hub.getCity())
                        .longitude(result.getContent().getPoint().getX())
                        .latitude(result.getContent().getPoint().getY())
                        .distance(result.getDistance().getValue())
                        .build());
            }
        }
        return nearestHubs;
    }

    public String getHubName(Long hubId) {
        return hubRepository.findByHubId(hubId)
                .map(Hub::getName)
                .orElse("Unknown Hub");
    }

    public Map<Long, String> getHubNames(Collection<Long> hubIds) {
        return hubRepository.findAllByHubIdIn(hubIds).stream()
                .collect(Collectors.toMap(Hub::getHubId, Hub::getName));
    }
}
