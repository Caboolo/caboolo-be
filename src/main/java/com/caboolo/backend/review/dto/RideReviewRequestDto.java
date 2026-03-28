package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideReviewRequestDto {
    private Long rideId;
    private String source;
    private String destination;
    private List<MinProfileDto> riders;

    public static interface RideIdStep {
        SourceStep withRideId(Long rideId);
    }

    public static interface SourceStep {
        DestinationStep withSource(String source);
    }

    public static interface DestinationStep {
        RidersStep withDestination(String destination);
    }

    public static interface RidersStep {
        BuildStep withRiders(List<MinProfileDto> riders);
    }

    public static interface BuildStep {
        RideReviewRequestDto build();
    }


    public static class Builder implements RideIdStep, SourceStep, DestinationStep, RidersStep, BuildStep {
        private Long rideId;
        private String source;
        private String destination;
        private List<MinProfileDto> riders;

        private Builder() {
        }

        public static RideIdStep rideReviewRequestDto() {
            return new Builder();
        }

        @Override
        public SourceStep withRideId(Long rideId) {
            this.rideId = rideId;
            return this;
        }

        @Override
        public DestinationStep withSource(String source) {
            this.source = source;
            return this;
        }

        @Override
        public RidersStep withDestination(String destination) {
            this.destination = destination;
            return this;
        }

        @Override
        public BuildStep withRiders(List<MinProfileDto> riders) {
            this.riders = riders;
            return this;
        }

        @Override
        public RideReviewRequestDto build() {
            return new RideReviewRequestDto(
                    this.rideId,
                    this.source,
                    this.destination,
                    this.riders
            );
        }
    }
}
