package com.caboolo.backend.hub.controller;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import com.caboolo.backend.hub.dto.HubDto;
import com.caboolo.backend.hub.service.HubService;
import com.caboolo.backend.hub.util.ExcelParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/hubs")
public class HubController extends BaseController {

    private final HubService hubService;

    public HubController(HubService hubService) {
        this.hubService = hubService;
    }

    @PostMapping(value = "/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RestEntity<Void> bulkStoreHubs(@RequestParam("file") MultipartFile file) {
        log.info("Received bulk hub upload request, fileName={}", file.getOriginalFilename());
        try {
            List<HubDto> hubs = ExcelParserUtil.parseHubs(file.getInputStream());
            log.info("Parsed {} hubs from Excel file", hubs.size());
            hubService.bulkStoreHubs(hubs);
            log.info("Bulk hub upload completed successfully");
            return successResponse("Hubs stored successfully in Redis and MySQL from Excel");
        } catch (Exception e) {
            log.error("Failed to process bulk hub Excel file: {}", e.getMessage(), e);
            return errorResponse("Failed to process Excel file: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public RestEntity<List<HubDto>> getAllHubs() {
        log.info("Fetching all hubs");
        List<HubDto> hubs = hubService.getAllHubs();
        log.info("Returned {} hubs", hubs.size());
        return successResponse(hubs, "All hubs retrieved successfully");
    }

    @GetMapping("/preferred")
    public RestEntity<List<HubDto>> getHubsByPriority(
            @RequestParam(defaultValue = "1") int minPriority,
            @RequestParam(defaultValue = "7") int maxPriority) {
        log.info("Fetching hubs by priority range [{}, {}]", minPriority, maxPriority);
        List<HubDto> hubs = hubService.getHubsByPriority(minPriority, maxPriority);
        log.info("Returned {} hubs for priority range [{}, {}]", hubs.size(), minPriority, maxPriority);
        return successResponse(hubs, "Hubs by priority retrieved successfully from cache");
    }

    @GetMapping("/nearest")
    public RestEntity<List<HubDto>> getNearestHubs(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "10.0") double radiusKm) {
        log.info("Finding nearest hubs for coordinates [{}, {}] within {} km", latitude, longitude, radiusKm);
        List<HubDto> nearestHubs = hubService.findNearestHubs(longitude, latitude, radiusKm);
        log.info("Found {} nearest hubs", nearestHubs.size());
        return successResponse(nearestHubs, "Nearest hubs retrieved successfully");
    }
}
