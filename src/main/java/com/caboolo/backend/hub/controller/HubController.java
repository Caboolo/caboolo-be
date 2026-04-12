package com.caboolo.backend.hub.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.hub.dto.HubDto;
import com.caboolo.backend.hub.service.HubService;
import com.caboolo.backend.hub.util.ExcelParserUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hubs")
public class HubController extends BaseController {

    private final HubService hubService;

    public HubController(HubService hubService) {
        this.hubService = hubService;
    }

    @PostMapping("/bulk")
    public RestEntity<Void> bulkStoreHubs(@RequestParam("file") MultipartFile file) {
        try {
            List<HubDto> hubs = ExcelParserUtil.parseHubs(file.getInputStream());
            hubService.bulkStoreHubs(hubs);
            return successResponse("Hubs stored successfully in Redis and MySQL from Excel");
        } catch (Exception e) {
            return errorResponse("Failed to process Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public RestEntity<List<HubDto>> getAllHubs() {
        return successResponse(hubService.getAllHubs(), "All hubs retrieved successfully");
    }

    @GetMapping("/preferred")
    public RestEntity<List<HubDto>> getHubsByPriority(
            @RequestParam(defaultValue = "1") int minPriority,
            @RequestParam(defaultValue = "7") int maxPriority) {
        List<HubDto> hubs = hubService.getHubsByPriority(minPriority, maxPriority);
        return successResponse(hubs, "Hubs by priority retrieved successfully from cache");
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
