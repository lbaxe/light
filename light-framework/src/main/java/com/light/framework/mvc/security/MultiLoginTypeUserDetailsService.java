package com.light.framework.mvc.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface MultiLoginTypeUserDetailsService {
    UserDetails loadUserByUsername(String username) throws UserNotFoundException;

    UserDetails loadUserByMobilephone(String mobilephone) throws UserNotFoundException;

    UserDetails loadUserByMail(String mail) throws UserNotFoundException;

    UserDetails loadUserByToken(String token) throws UserNotFoundException;
}
