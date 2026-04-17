package org.maldeclabs.spider.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.maldeclabs.spider.domain.enums.SubscriptionType;

@Table(name = "stripe_plan_details")
@Entity(name = "StripePlanDetails")
@Getter
@Setter
@NoArgsConstructor
public class StripePlanDetails {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "plan_type")
    private String planType; // FREE, INDIVIDUAL, ENTERPRISE

    @Column(name = "subscription_type")
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType; // MONTHLY, YEARLY

    public StripePlanDetails(String planType, SubscriptionType subscriptionType) {
        this.planType = planType;
        this.subscriptionType = subscriptionType;
    }
}
