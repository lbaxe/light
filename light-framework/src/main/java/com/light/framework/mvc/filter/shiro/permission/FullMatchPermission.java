package com.light.framework.mvc.filter.shiro.permission;

import org.apache.shiro.authz.Permission;

public class FullMatchPermission implements Permission {
    protected static final boolean DEFAULT_CASE_SENSITIVE = false;
    private String permissionString;

    @Override
    public boolean implies(Permission p) {
        return this.equals(p);
    }

    protected FullMatchPermission() {}

    public FullMatchPermission(String permissionString) {
        this(permissionString, DEFAULT_CASE_SENSITIVE);
    }

    public FullMatchPermission(String permissionString, boolean caseSensitive) {
        if (permissionString == null || permissionString.isEmpty()) {
            throw new IllegalArgumentException(
                "FullMatchPermission string cannot be null or empty. Make sure permission strings are properly formatted.");
        }
        this.permissionString = caseSensitive ? permissionString : permissionString.toLowerCase();
    }

    @Override
    public String toString() {
        return permissionString;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof FullMatchPermission) {
            FullMatchPermission permission = (FullMatchPermission)o;
            return permissionString.equals(permission.permissionString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return permissionString.hashCode();
    }
}
