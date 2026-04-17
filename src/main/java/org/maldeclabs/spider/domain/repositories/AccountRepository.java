package org.maldeclabs.spider.domain.repositories;

import org.maldeclabs.spider.domain.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("SELECT ac FROM account ac JOIN FETCH ac.emailConfirmation ec WHERE ac.email = :email")
    Account findByEmail(String email);

    @Query("SELECT ac FROM account ac JOIN FETCH ac.profile p WHERE ac.profile = :profile")
    Account findByProfile(String profile);

    @Query("SELECT ac FROM account ac JOIN ac.emailForgotPasswords efp WHERE efp.token = :token")
    Account findByEFPToken(@Param("token") String token);

    @Query("SELECT CASE WHEN COUNT(ac) > 0 THEN true ELSE false END FROM account ac WHERE ac.profile = :profile")
    boolean existsByProfile(String profile);

    @Query("SELECT CASE WHEN COUNT(ac) > 0 THEN true ELSE false END FROM account ac WHERE ac.email = :email")
    boolean existsByEmail(String email);
}
