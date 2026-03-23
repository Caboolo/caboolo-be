package com.caboolo.backend.cloudinary;

import com.caboolo.backend.core.controller.BaseController;
import com.caboolo.backend.core.dto.RestEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/cloudinary")
public class CloudinaryController extends BaseController {

    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public RestEntity<String> uploadImage(@RequestParam("file") MultipartFile file, 
                                          @RequestParam(value = "folder", defaultValue = "caboolo_uploads") String folder) {
        try {
            String url = cloudinaryService.uploadFile(file, folder);
            return successResponse(url, "Image uploaded successfully");
        } catch (IOException e) {
            return errorResponse("Failed to upload image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/resolve")
    public RestEntity<String> resolveImage() {
        // Since we aren't saving it in a DB yet, we hardcode the publicId here as requested
        String hardcodedPublicId = "caboolo_uploads/sample_upload";
        
        // Fetch/generate the URL from Cloudinary using the SDK
        String resolvedUrl = cloudinaryService.resolveImage(hardcodedPublicId);
        
        return successResponse(resolvedUrl, "Image URL resolved successfully");
    }
}
