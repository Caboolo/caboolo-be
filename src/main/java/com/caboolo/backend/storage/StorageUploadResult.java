package com.caboolo.backend.storage;

/**
 * Immutable result returned by {@link StorageService#upload}.
 * <p>
 * Carrying both the public URL and the provider-specific public ID here keeps
 * the service layer free of any provider-specific URL-parsing logic.
 * </p>
 */
public final class StorageUploadResult {

    /** Public CDN URL of the uploaded file. */
    private final String url;

    /**
     * Provider-specific identifier needed to delete the file later.
     * <ul>
     *   <li>Cloudinary: the {@code public_id} field from the upload response
     *       (e.g. {@code caboolo/profile_photos/abc123})</li>
     *   <li>S3: the full object key (e.g. {@code caboolo/profile_photos/abc123.jpg})</li>
     * </ul>
     */
    private final String publicId;

    public StorageUploadResult(String url, String publicId) {
        this.url = url;
        this.publicId = publicId;
    }

    public String getUrl() {
        return url;
    }

    public String getPublicId() {
        return publicId;
    }
}
