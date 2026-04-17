package org.maldeclabs.spider.domain.repositories;

import org.maldeclabs.spider.domain.entities.StripeSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StripeSubscriptionRepository extends JpaRepository<StripeSubscription, String> {
    StripeSubscription findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<StripeSubscription> findById(String id);
}
