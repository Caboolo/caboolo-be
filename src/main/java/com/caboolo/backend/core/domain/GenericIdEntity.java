package com.caboolo.backend.core.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class GenericIdEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date_created", nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime dateCreated;

    @Column(name = "last_modified", nullable = false)
    @LastModifiedDate
    private LocalDateTime lastModified;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

}
