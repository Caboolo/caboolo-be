package com.caboolo.backend.hub.dto;

import com.caboolo.backend.hub.enums.City;
import com.caboolo.backend.hub.enums.HubType;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HubDto {
    private Long hubId;
    private String name;
    private HubType type;
    private City city;
    private Integer priority;
    private Double longitude;
    private Double latitude;
    private Double distance; // For search results
}
