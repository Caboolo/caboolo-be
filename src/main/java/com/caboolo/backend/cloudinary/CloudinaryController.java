package com.caboolo.backend.cloudinary;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/cloudinary")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, 
                                              @RequestParam(value = "folder", defaultValue = "caboolo_uploads") String folder) {
        try {
            String url = cloudinaryService.uploadFile(file, folder);
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }

    @GetMapping("/resolve")
    public ResponseEntity<String> resolveImage() {
        // Since we aren't saving it in a DB yet, we hardcode the publicId here as requested
        String hardcodedPublicId = "caboolo_uploads/sample_upload";
        
        // Fetch/generate the URL from Cloudinary using the SDK
        String resolvedUrl = cloudinaryService.resolveImage(hardcodedPublicId);
        
        return ResponseEntity.ok(resolvedUrl);
    }
}
