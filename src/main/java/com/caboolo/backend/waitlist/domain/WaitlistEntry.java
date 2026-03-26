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

    @Id
    @GeneratedValue(generator = "entity-unique-id-generator")
    @org.hibernate.annotations.GenericGenerator(
        name = "entity-unique-id-generator",
        type = com.caboolo.backend.core.idgen.EntityUniqueIdGenerator.class
    )
    @Column(name = "waitlist_entry_id")
    private Long waitlistEntryId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    public static interface EmailStep {
        BuildStep withEmail(String email);
    }

    public static interface BuildStep {
        WaitlistEntry build();
    }

    public static class Builder implements EmailStep, BuildStep {
        private String email;

        private Builder() {
        }

        public static EmailStep waitlistEntry() {
            return new Builder();
        }

        @Override
        public BuildStep withEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public WaitlistEntry build() {
            return new WaitlistEntry(
                    this.email
            );
        }
    }
}
