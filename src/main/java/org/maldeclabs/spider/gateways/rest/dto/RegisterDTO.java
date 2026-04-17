package org.maldeclabs.spider.gateways.rest.dto;


import org.maldeclabs.spider.domain.validators.ValidEmail;
import org.maldeclabs.spider.domain.validators.ValidName;
import org.maldeclabs.spider.domain.validators.ValidPassword;
import org.maldeclabs.spider.domain.validators.ValidProfile;

public record RegisterDTO(
        @ValidName
        String name,

        @ValidProfile
        String profile,

        @ValidEmail
        String email,

        @ValidPassword
        String password
) {
}
