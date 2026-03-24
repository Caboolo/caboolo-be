package com.caboolo.backend.waitlist.domain;

import jakarta.persistence.Entity;
import com.caboolo.backend.core.domain.GenericIdEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "waitlist_entry")
public class WaitlistEntry extends GenericIdEntity {

    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    public WaitlistEntry() {}

    public WaitlistEntry(String email) {
        this.email = email;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
