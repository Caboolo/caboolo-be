package com.caboolo.backend.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Cloudinary-backed implementation of {@link StorageService}.
 * Active when {@code storage.provider=cloudinary} (or when the property is absent — default).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary", matchIfMissing = true)
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public StorageUploadResult upload(MultipartFile file, String folder) {
        log.info("Uploading file '{}' to Cloudinary folder '{}'", file.getOriginalFilename(), folder);
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto"
                )
            );
            String url = result.get("secure_url").toString();
            String publicId = result.get("public_id").toString();
            log.info("File uploaded successfully to Cloudinary: publicId={}, url={}", publicId, url);
            return new StorageUploadResult(url, publicId);
        } catch (IOException e) {
            log.error("Cloudinary upload failed for file '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String publicId) {
        log.info("Deleting file from Cloudinary: publicId={}", publicId);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted successfully from Cloudinary: publicId={}", publicId);
        } catch (IOException e) {
            log.error("Cloudinary delete failed for publicId='{}': {}", publicId, e.getMessage(), e);
            throw new RuntimeException("Cloudinary delete failed: " + e.getMessage(), e);
        }
    }
}
