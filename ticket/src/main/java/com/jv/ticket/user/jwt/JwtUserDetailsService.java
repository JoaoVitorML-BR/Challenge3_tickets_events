package com.jv.ticket.user.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jv.ticket.user.models.User;
import com.jv.ticket.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(email);
        return new JwtUserDetails(user);
    }

    public JwtToken getTokenAuthenticated(String email) {
        User user = userService.getUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com email: " + email);
        }

        return JwtUtils.createToken(user.getEmail(), user.getRole().name().substring("ROLE_".length()));
    }
}
