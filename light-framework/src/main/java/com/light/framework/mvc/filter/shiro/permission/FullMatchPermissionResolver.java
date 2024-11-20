package com.light.framework.mvc.filter.shiro.permission;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

public class FullMatchPermissionResolver implements PermissionResolver {
    boolean caseSensitive;

    public FullMatchPermissionResolver(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public FullMatchPermissionResolver() {
        this(FullMatchPermission.DEFAULT_CASE_SENSITIVE);
    }

    public void setCaseSensitive(boolean state) {
        this.caseSensitive = state;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public Permission resolvePermission(String permissionString) {
        return new FullMatchPermission(permissionString, caseSensitive);
    }
}
