package com.caboolo.backend.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoPassengerResponseDto {
    private String fromLocation;
    private String toLocation;
    private List<RiderDto> riders;

    public static interface FromLocationStep {
        ToLocationStep withFromLocation(String fromLocation);
    }

    public static interface ToLocationStep {
        RidersStep withToLocation(String toLocation);
    }

    public static interface RidersStep {
        BuildStep withRiders(List<RiderDto> riders);
    }

    public static interface BuildStep {
        CoPassengerResponseDto build();
    }

    public static class Builder implements FromLocationStep, ToLocationStep, RidersStep, BuildStep {
        private String fromLocation;
        private String toLocation;
        private List<RiderDto> riders;

        public static FromLocationStep builder() {
            return new Builder();
        }

        @Override
        public ToLocationStep withFromLocation(String fromLocation) {
            this.fromLocation = fromLocation;
            return this;
        }

        @Override
        public RidersStep withToLocation(String toLocation) {
            this.toLocation = toLocation;
            return this;
        }

        @Override
        public BuildStep withRiders(List<RiderDto> riders) {
            this.riders = riders;
            return this;
        }

        @Override
        public CoPassengerResponseDto build() {
            return new CoPassengerResponseDto(fromLocation, toLocation, riders);
        }
    }
}
