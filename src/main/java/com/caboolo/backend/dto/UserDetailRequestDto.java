package com.caboolo.backend.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import java.time.LocalDateTime;

import com.caboolo.backend.userdetails.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating a user's profile details.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailRequestDto extends GenericEntityDto {

    private String userId;

    private Gender gender;

    private String name;

    private String email;

    private String imageUrl;

    private String phoneNumber;

    public UserDetailRequestDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, String name, String email) {
        super(dateCreated, lastModified, isDeleted);
        this.name = name;
        this.email = email;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        NameStep withIsDeleted(boolean isDeleted);
    }

    public static interface NameStep {
        EmailStep withName(String name);
    }

    public static interface EmailStep {
        BuildStep withEmail(String email);
    }

    public static interface BuildStep {
        UserDetailRequestDto build();
    }


    public static class Builder implements DateCreatedStep, LastModifiedStep, IsDeletedStep, NameStep, EmailStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private String name;
        private String email;

        private Builder() {
        }

        public static DateCreatedStep userDetailRequestDto() {
            return new Builder();
        }

        @Override
        public LastModifiedStep withDateCreated(LocalDateTime dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        @Override
        public IsDeletedStep withLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        @Override
        public NameStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public EmailStep withName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public BuildStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public UserDetailRequestDto build() {
            return new UserDetailRequestDto(
                    this.dateCreated,
                    this.lastModified,
                    this.isDeleted,
                    this.name,
                    this.email
            );
        }
    }
}
