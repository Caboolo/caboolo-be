package com.caboolo.backend.about.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AboutInfoDto {
    private String version;
    private String supportEmail;
    private String websiteUrl;
    private String termsOfServiceUrl;
    private String privacyPolicyUrl;
}
