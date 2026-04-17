package org.maldeclabs.spider.domain.repositories;

import org.maldeclabs.spider.domain.entities.EmailForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailForgotPasswordRepository extends JpaRepository<EmailForgotPassword, String> {

    @Query("SELECT efp FROM EmailForgotPassword efp WHERE efp.token = :token")
    EmailForgotPassword findByToken(String token);
}
