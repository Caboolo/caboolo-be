package com.caboolo.backend.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class GenericEntityDto {

    private LocalDateTime dateCreated;

    private LocalDateTime lastModified;

    private boolean isDeleted;

}
