package com.caboolo.backend.waitlist.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.caboolo.backend.core.domain.GenericIdEntity;

@Entity
@Table(name = "waitlist_entry")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WaitlistEntry extends GenericIdEntity {

    @Column(name = "waitlist_entry_id")
    private String waitlistEntryId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    public static interface WaitlistEntryIdStep {
        EmailStep withWaitlistEntryId(String waitlistEntryId);
    }

    public static interface EmailStep {
        BuildStep withEmail(String email);
    }

    public static interface BuildStep {
        WaitlistEntry build();
    }

    public static class Builder implements WaitlistEntryIdStep, EmailStep, BuildStep {
        private String waitlistEntryId;
        private String email;

        private Builder() {
        }

        public static WaitlistEntryIdStep waitlistEntry() {
            return new Builder();
        }

        @Override
        public EmailStep withWaitlistEntryId(String waitlistEntryId) {
            this.waitlistEntryId = waitlistEntryId;
            return this;
        }

        @Override
        public BuildStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public WaitlistEntry build() {
            return new WaitlistEntry(
                    this.waitlistEntryId,
                    this.email
            );
        }
    }
}
