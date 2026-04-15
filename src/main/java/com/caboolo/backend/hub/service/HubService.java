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
import org.springframework.data.redis.core.ZSetOperations;
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
    private final ZSetOperations<String, String> zSetOps;
    private final SequenceGenerator sequenceGenerator;

    public HubService(HubRepository hubRepository, GeoOperations<String, String> geoOps,
                      ZSetOperations<String, String> zSetOps, SequenceGenerator sequenceGenerator) {
        this.hubRepository = hubRepository;
        this.geoOps = geoOps;
        this.zSetOps = zSetOps;
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
                        .withPriority(dto.getPriority())
                        .withLatitude(dto.getLatitude())
                        .withLongitude(dto.getLongitude())
                        .build())
                .collect(Collectors.toList());

        // 1. Store in MySQL
        hubRepository.saveAll(hubs);

        // 2. Store in Redis GEO
        List<RedisGeoCommands.GeoLocation<String>> locations = hubs.stream()
                .map(hub -> new RedisGeoCommands.GeoLocation<>(
                        hub.getHubId().toString(),
                        new Point(hub.getLongitude(), hub.getLatitude())
                ))
                .collect(Collectors.toList());

        geoOps.add(REDIS_HUB_KEY, locations);
        log.info("Bulk stored {} hubs in Redis and MySQL", hubs.size());
    }

    public List<HubDto> findNearestHubs(double longitude, double latitude, double radiusKm) {
        log.info("Searching for nearest hubs at coordinates [{}, {}] within {} km", latitude, longitude, radiusKm);
        Point center = new Point(longitude, latitude);
        Distance radius = new Distance(radiusKm, RedisGeoCommands.DistanceUnit.KILOMETERS);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        // Use Circle for searching from a point instead of a member
        RedisGeoCommands.GeoRadiusCommandArgs geoArgs =
            RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .includeCoordinates()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = geoOps.radius(REDIS_HUB_KEY, new org.springframework.data.geo.Circle(center, radius), geoArgs);

        if (results == null || results.getContent().isEmpty()) {
            log.warn("No hubs found within {} km of [{}, {}]", radiusKm, latitude, longitude);
            return new ArrayList<>();
        }

        List<Long> nearestIds = results.getContent().stream()
                .map(res -> Long.valueOf(res.getContent().getName()))
                .collect(Collectors.toList());

        // Fetch details from MySQL using hubIds
        Map<Long, Hub> hubMap = hubRepository.findByHubIdIn(nearestIds).stream()
                .collect(Collectors.toMap(Hub::getHubId, h -> h));

        List<HubDto> nearestHubs = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            Long hubId = Long.valueOf(result.getContent().getName());
            Hub hub = hubMap.get(hubId);

            if (hub != null) {
                nearestHubs.add(HubDto.builder()
                        .name(hub.getName())
                        .type(hub.getType())
                        .city(hub.getCity())
                        .priority(hub.getPriority())
                        .longitude(result.getContent().getPoint().getX())
                        .latitude(result.getContent().getPoint().getY())
                        .distance(result.getDistance().getValue())
                        .build());
            }
        }
        log.info("Found {} hubs near [{}, {}]", nearestHubs.size(), latitude, longitude);
        return nearestHubs;
    }

    public List<HubDto> getAllHubs() {
        // 1. Enumerate all hub IDs from Redis GEO (backed by a sorted set)
        java.util.Set<String> members = zSetOps.range(REDIS_HUB_KEY, 0, -1);
        if (members == null || members.isEmpty()) {
            log.warn("Hub cache is empty — returning empty list");
            return new ArrayList<>();
        }

        log.info("Fetching all hubs, found {} members in Redis cache", members.size());
        List<Long> hubIds = members.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 2. Fetch coordinates from Redis in one call
        List<String> memberList = members.stream().toList();
        List<Point> positions = geoOps.position(REDIS_HUB_KEY,
                memberList.toArray(new String[0]));

        // 3. Bulk-resolve name / type / city from DB
        Map<Long, Hub> hubMap = hubRepository.findByHubIdIn(hubIds).stream()
                .collect(Collectors.toMap(Hub::getHubId, h -> h));

        List<HubDto> result = new ArrayList<>();
        for (int i = 0; i < memberList.size(); i++) {
            Long hubId = Long.valueOf(memberList.get(i));
            Hub hub = hubMap.get(hubId);
            Point point = (positions != null) ? positions.get(i) : null;
            if (hub == null) continue;
            result.add(HubDto.builder()
                    .name(hub.getName())
                    .type(hub.getType())
                    .city(hub.getCity())
                    .priority(hub.getPriority())
                    .longitude(point != null ? point.getX() : hub.getLongitude())
                    .latitude(point != null ? point.getY() : hub.getLatitude())
                    .build());
        }
        log.info("getAllHubs returning {} hub(s)", result.size());
        return result;
    }

    public List<HubDto> getHubsByPriority(int minPriority, int maxPriority) {
        return getAllHubs().stream()
                .filter(hub -> hub.getPriority() != null && hub.getPriority() >= minPriority && hub.getPriority() <= maxPriority)
                .collect(Collectors.toList());
    }

    public Map<Long, String> getHubNames(Collection<Long> hubIds) {
        return hubRepository.findByHubIdIn(hubIds).stream()
                .collect(Collectors.toMap(Hub::getHubId, Hub::getName));
    }

    public Map<Long, Hub> getHubsByIds(Collection<Long> hubIds) {
        return hubRepository.findByHubIdIn(hubIds).stream()
                .collect(Collectors.toMap(Hub::getHubId, h -> h));
    }

    public Map<Long, String> getHubsMap(Collection<Long> hubIds) {
        return hubRepository.findHubIdAndNameByHubIdIn(hubIds);
    }
}
