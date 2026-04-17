package org.maldeclabs.spider.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name="email_confirmation")
@Entity(name="EmailConfirmation")
@Getter
@Setter
@NoArgsConstructor
public class EmailConfirmation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "code")
    private String code;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public EmailConfirmation(Boolean enabled, String code, LocalDateTime expiresAt){
        this.enabled = enabled;
        this.code = code;
        this.expiresAt = expiresAt;
    }

}
