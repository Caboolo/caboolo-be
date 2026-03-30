package com.caboolo.backend.ride.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassengerInfoDto {
    private String userId;
    private String name;
    private String imageUrl;
    private Double avgRating;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String name;
        private String imageUrl;
        private Double avgRating;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder avgRating(Double avgRating) { this.avgRating = avgRating; return this; }
        public PassengerInfoDto build() {
            return new PassengerInfoDto(userId, name, imageUrl, avgRating);
        }
    }
}
