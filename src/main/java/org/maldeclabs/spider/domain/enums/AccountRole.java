package org.maldeclabs.spider.domain.enums;

public enum AccountRole {
    ADMIN("admin"),
    FREE("free"),
    BASIC("basic"),
    BUSINESS("business");

    private final String role;

    AccountRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
