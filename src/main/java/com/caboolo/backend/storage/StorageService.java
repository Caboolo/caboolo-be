package com.caboolo.backend.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Provider-agnostic file storage abstraction.
 * <p>
 * To switch providers (e.g. Cloudinary → S3):
 * 1. Add a new implementation of this interface.
 * 2. Set {@code storage.provider=<new-provider>} in application.properties.
 * No other code changes needed.
 * </p>
 */
public interface StorageService {

    /**
     * Upload a file to the given folder/prefix.
     *
     * @param file   the multipart file to upload
     * @param folder logical folder / S3 key prefix
     * @return a {@link StorageUploadResult} containing the public CDN URL and the
     *         provider-specific public ID required for future deletion
     */
    StorageUploadResult upload(MultipartFile file, String folder);

    /**
     * Delete a previously uploaded file.
     *
     * @param publicId the provider-specific public identifier returned at upload time
     */
    void delete(String publicId);
}
