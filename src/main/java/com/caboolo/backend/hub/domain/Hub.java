package com.caboolo.backend.hub.domain;

import com.caboolo.backend.core.domain.GenericIdEntity;
import com.caboolo.backend.hub.enums.City;
import com.caboolo.backend.hub.enums.HubType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hubs")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hub extends GenericIdEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private HubType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "city", nullable = false)
    private City city;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

}
