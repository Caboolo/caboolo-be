package com.caboolo.backend.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto extends GenericEntityDto {

    private String idToken;
    private String phoneNumber;

    public LoginRequestDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, String idToken) {
        super(dateCreated, lastModified, isDeleted);
        this.idToken = idToken;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        IdTokenStep withIsDeleted(boolean isDeleted);
    }

    public static interface IdTokenStep {
        BuildStep withIdToken(String idToken);
    }

    public static interface BuildStep {
        LoginRequestDto build();
    }


    public static class Builder implements DateCreatedStep, LastModifiedStep, IsDeletedStep, IdTokenStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private String idToken;

        private Builder() {
        }

        public static DateCreatedStep loginRequestDto() {
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
        public IdTokenStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public BuildStep withIdToken(String idToken) {
            this.idToken = idToken;
            return this;
        }

        @Override
        public LoginRequestDto build() {
            return new LoginRequestDto(
                    this.dateCreated,
                    this.lastModified,
                    this.isDeleted,
                    this.idToken
            );
        }
    }
}
