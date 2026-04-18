package com.caboolo.backend.hub.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.hub.enums.City;
import com.caboolo.backend.hub.enums.HubType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hub")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Hub extends GenericIdEntity {

    @Column(name = "hub_id")
    private String hubId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HubType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "city", nullable = false)
    private City city;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    public static interface HubIdStep {
        NameStep withHubId(String hubId);
    }

    public static interface NameStep {
        TypeStep withName(String name);
    }

    public static interface TypeStep {
        CityStep withType(HubType type);
    }

    public static interface CityStep {
        PriorityStep withCity(City city);
    }

    public static interface PriorityStep {
        LatitudeStep withPriority(Integer priority);
    }

    public static interface LatitudeStep {
        LongitudeStep withLatitude(Double latitude);
    }

    public static interface LongitudeStep {
        BuildStep withLongitude(Double longitude);
    }

    public static interface BuildStep {
        Hub build();
    }


    public static class Builder implements HubIdStep, NameStep, TypeStep, CityStep, PriorityStep, LatitudeStep, LongitudeStep, BuildStep {
        private String hubId;
        private String name;
        private HubType type;
        private City city;
        private Integer priority;
        private Double latitude;
        private Double longitude;

        private Builder() {
        }

        public static HubIdStep hub() {
            return new Builder();
        }

        @Override
        public NameStep withHubId(String hubId) {
            this.hubId = hubId;
            return this;
        }

        @Override
        public TypeStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public CityStep withType(HubType type) {
            this.type = type;
            return this;
        }

        @Override
        public PriorityStep withCity(City city) {
            this.city = city;
            return this;
        }

        @Override
        public LatitudeStep withPriority(Integer priority) {
            this.priority = priority;
            return this;
        }

        @Override
        public LongitudeStep withLatitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        @Override
        public BuildStep withLongitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        @Override
        public Hub build() {
            return new Hub(
                    this.hubId,
                    this.name,
                    this.type,
                    this.city,
                    this.priority,
                    this.latitude,
                    this.longitude
            );
        }
    }
}
