package org.maldeclabs.spider.domain.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name="email_forgot_password")
@Entity(name="EmailForgotPassword")
@Getter
@Setter
@NoArgsConstructor
public class EmailForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "old_password")
    private String oldPassword;

    @Column(name = "token")
    private String token;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public EmailForgotPassword(String oldPassword, String token, LocalDateTime expiresAt){
        this.oldPassword = oldPassword;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public EmailForgotPassword(String token, LocalDateTime expiresAt){
        this.token = token;
        this.expiresAt = expiresAt;
    }

}