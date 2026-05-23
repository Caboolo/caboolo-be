package com.caboolo.backend.notification.dto;

import com.caboolo.backend.notification.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FcmTokenRequestDto {
    @NotBlank
    private String fcmToken;
    @NotBlank
    private String deviceId;
    private DeviceType deviceType = DeviceType.UNKNOWN;
    private String appVersion;
}
