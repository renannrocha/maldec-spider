package org.maldeclabs.spider.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {
    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // rest
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/validate-jwt").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/resend-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/confirm-email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/request-forgot-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/forgot-password").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/account").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.DELETE, "/api/account/delete").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.PUT, "/api/account/update").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.PUT, "/api/account/update-role").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.PUT, "/api/account/update-password").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.PUT, "/api/account/update-integrations").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.POST, "/api/rabbit/file/search").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/rabbit/file/account/search").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.PUT, "/api/rabbit/file/account/update").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.DELETE, "/api/rabbit/file/account/delete").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")

                        .requestMatchers(HttpMethod.POST, "/api/stripe/create-checkout-session").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/stripe/subscriptions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stripe/get/subscription").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stripe/invoice/pdf").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/stripe/delete/subscription").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/stripe/update/subscription").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/subscriptions").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/download/infinity/pro/generate-build").hasAnyRole("ADMIN", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.GET, "/api/download/deb/infinity/demo").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/download/gz/infinity/demo").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/download/deb/infinity/pro").hasAnyRole("ADMIN", "BASIC", "BUSINESS")
                        .requestMatchers(HttpMethod.GET, "/api/download/gz/infinity/pro").hasAnyRole("ADMIN", "BASIC", "BUSINESS")

                        // websocket
                        .requestMatchers("/v1/skull/secured/analysis/scan").hasAnyRole("ADMIN", "FREE", "BASIC", "BUSINESS")
                        .requestMatchers("/v1/skull/data/metadata").hasAnyRole("ADMIN")
                        .requestMatchers("/v1/skull/analysis/scan/yara").hasAnyRole("ADMIN")
                        .requestMatchers("/v1/skull/analysis/scan/av/clamav").hasAnyRole("ADMIN")
                        .requestMatchers("/v1/skull/analysis/quick/scan").permitAll()
                        .requestMatchers("/v1/skull/parser/binary/dex").permitAll()
                        .requestMatchers("/v1/skull/parser/binary/elf").permitAll()
                        .requestMatchers("/v1/skull/parser/binary/macho").permitAll()
                        .requestMatchers("/v1/skull/parser/binary/pe").permitAll()
                        .requestMatchers(HttpMethod.POST,"/webhooks").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
