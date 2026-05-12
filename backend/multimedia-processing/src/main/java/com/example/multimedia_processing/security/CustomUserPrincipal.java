package com.example.multimedia_processing.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {

    private final String email;
    private final String password;
    private final String userId;
    private final Collection<? extends GrantedAuthority> authorities;

    @NotNull
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}