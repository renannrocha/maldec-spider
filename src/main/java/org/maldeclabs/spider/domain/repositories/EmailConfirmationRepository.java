package org.maldeclabs.spider.domain.repositories;

import org.maldeclabs.spider.domain.entities.EmailConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, String> {

    @Query("SELECT ec FROM EmailConfirmation ec WHERE ec.code = :code")
    EmailConfirmation findByCode(String code);
}
