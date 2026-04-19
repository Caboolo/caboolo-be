package com.caboolo.backend.waitlist.dto;

import com.caboolo.backend.core.dto.GenericEntityDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaitlistRequestDto extends GenericEntityDto {
    @NotBlank(message = "email cannot be null or blank")
    @Email(message = "email must be a valid email address")
    private String email;

    public WaitlistRequestDto(LocalDateTime dateCreated, LocalDateTime lastModified, boolean isDeleted, String email) {
        super(dateCreated, lastModified, isDeleted);
        this.email = email;
    }

    public static interface DateCreatedStep {
        LastModifiedStep withDateCreated(LocalDateTime dateCreated);
    }

    public static interface LastModifiedStep {
        IsDeletedStep withLastModified(LocalDateTime lastModified);
    }

    public static interface IsDeletedStep {
        EmailStep withIsDeleted(boolean isDeleted);
    }

    public static interface EmailStep {
        BuildStep withEmail(String email);
    }

    public static interface BuildStep {
        WaitlistRequestDto build();
    }

    public static class Builder implements DateCreatedStep, LastModifiedStep, IsDeletedStep, EmailStep, BuildStep {
        private LocalDateTime dateCreated;
        private LocalDateTime lastModified;
        private boolean isDeleted;
        private String email;

        private Builder() {}

        public static DateCreatedStep waitlistRequestDto() {
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
        public EmailStep withIsDeleted(boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Override
        public BuildStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public WaitlistRequestDto build() {
            return new WaitlistRequestDto(
                this.dateCreated,
                this.lastModified,
                this.isDeleted,
                this.email
            );
        }
    }
}
