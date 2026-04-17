package org.maldeclabs.spider.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Table(name = "stripe_subscription")
@Entity(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
public class StripeSubscription {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private String id;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "status")
    private String status;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "payment_method_type")
    private List<String> paymentMethodType;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stripe_plan_details_id", nullable = false)
    private StripePlanDetails stripePlanDetails;

    public StripeSubscription(String stripeSubscriptionId, String stripeCustomerId, String status, LocalDateTime startDate, LocalDateTime endDate, List<String> paymentMethodType, StripePlanDetails stripePlanDetails) {
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.stripeCustomerId = stripeCustomerId;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.paymentMethodType = paymentMethodType;
        this.stripePlanDetails = stripePlanDetails;
    }
}
