package org.maldeclabs.spider.gateways.rest.dto;

import org.maldeclabs.spider.domain.entities.StripeSubscription;
import org.maldeclabs.spider.domain.enums.AccountRole;

public record AccountDataResponseDTO(String name, String profile, String email, Boolean is_verified, AccountRole account_role, StripeSubscription subscription) {
}
