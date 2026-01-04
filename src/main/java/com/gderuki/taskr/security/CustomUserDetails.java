package com.gderuki.taskr.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom UserDetails impl that includes user ID.
 * Allows us to access the user ID from the security context for audit logging.
 */
@Getter
public class CustomUserDetails extends User {

    private final Long id;

    public CustomUserDetails(Long id, String username, String password, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.id = id;
    }

    public CustomUserDetails(Long id, String username, String password,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }
}
