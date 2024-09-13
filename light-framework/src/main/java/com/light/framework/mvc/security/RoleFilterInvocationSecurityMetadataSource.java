package com.light.framework.mvc.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.util.AntPathMatcher;

public class RoleFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private PermissionService permissionService;

    public RoleFilterInvocationSecurityMetadataSource(ObjectProvider<PermissionService> permissionService) {
        this.permissionService = permissionService.getIfAvailable();
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (object instanceof FilterInvocation) {
            FilterInvocation invocation = (FilterInvocation)object;

            String url = invocation.getRequestUrl();
            List<ConfigAttribute> attributes = new ArrayList<>();
            List<String> grantedRoles = null;
            if (permissionService != null) {
                grantedRoles = permissionService.loadGrantedAuthorities(invocation.getRequestUrl());
                return SecurityConfig.createList(grantedRoles.toArray(new String[grantedRoles.size()]));
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }
}
