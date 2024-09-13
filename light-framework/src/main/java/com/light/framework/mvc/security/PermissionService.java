package com.light.framework.mvc.security;

import java.util.List;

public interface PermissionService {
    /**
     * 加载uri对应的角色
     * 
     * @param uri
     * @return
     */
    List<String> loadGrantedAuthorities(String uri);
}
