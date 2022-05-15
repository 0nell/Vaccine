package org.hse.model;

public enum UserType {
    ADMIN("ADMIN"),
    USER("USER");

    private final String permission;

    private UserType(final String permission){this.permission=permission;}


    public String getPermission() {
        return this.permission;
    }
}
