package org.maldeclabs.spider.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.maldeclabs.spider.domain.enums.AccountRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name="account")
@Entity(name="account")
@Getter
@Setter
@NoArgsConstructor
public class Account implements UserDetails{

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "profile")
    private String profile;

    @Column(name = "email", unique = true)
    private String email;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @JsonIgnore
    private AccountRole role;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emailConfirmation_id", referencedColumnName = "id")
    private EmailConfirmation emailConfirmation;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "account_id")
    private Set<EmailForgotPassword> emailForgotPasswords = new HashSet<>();

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stripe_subscription_id", referencedColumnName = "id")
    private StripeSubscription stripeSubscription;

    public Account(String name, String email, String password, Set<EmailForgotPassword> emailForgotPassword) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.emailForgotPasswords = emailForgotPassword;
    }

    public Account(String name, String profile, String email, String password, AccountRole accountRole, EmailConfirmation emailConfirmation) {
        this.name = name;
        this.profile = profile;
        this.email = email;
        this.password = password;
        this.role = accountRole;
        this.emailConfirmation = emailConfirmation;
    }

    public Account(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public EmailForgotPassword getEmailForgotPasswordByToken(String token) {
        return this.emailForgotPasswords.stream()
                .filter(efp -> efp.getToken().equals(token))
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String roleName;
        switch (this.role) {
            case ADMIN -> roleName = "ROLE_ADMIN";
            case BUSINESS -> roleName = "ROLE_BUSINESS";
            case BASIC -> roleName = "ROLE_BASIC";
            default -> roleName = "ROLE_FREE";
        }
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return this.email;
    }

    @JsonIgnore
    @Override
    public String getPassword(){
        return password;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}