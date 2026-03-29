package com.caboolo.backend.hub.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.hub.dto.HubDto;
import com.caboolo.backend.hub.service.HubService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hubs")
public class HubController extends BaseController {

    private final HubService hubService;

    public HubController(HubService hubService) {
        this.hubService = hubService;
    }

    @PostMapping("/bulk")
    public RestEntity<Void> bulkStoreHubs(@RequestBody List<HubDto> hubs) {
        hubService.bulkStoreHubs(hubs);
        return successResponse("Hubs stored successfully in Redis and MySQL");
    }

    @GetMapping("/nearest")
    public RestEntity<List<HubDto>> getNearestHubs(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        List<HubDto> nearestHubs = hubService.findNearestHubs(longitude, latitude, radiusKm);
        return successResponse(nearestHubs, "Nearest hubs retrieved successfully");
    }
}
