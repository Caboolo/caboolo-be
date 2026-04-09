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
            return new StorageUploadResult(url, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary delete failed: " + e.getMessage(), e);
        }
    }
}
