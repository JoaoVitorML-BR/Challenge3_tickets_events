package com.jv.ticket.user.jwt;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class JwtUserDetails extends User {

    private com.jv.ticket.user.models.User user;

    public JwtUserDetails(com.jv.ticket.user.models.User userDetails) {
        super(userDetails.getEmail(), userDetails.getPassword(),
                AuthorityUtils.createAuthorityList(userDetails.getRole().name()));
        this.user = userDetails;
    }

    public String getId() {
        return this.user.getId();
    }

    public String getRole() {
        return this.user.getRole().name();
    }

    public String getCpf() {
        return this.user.getCpf();
    }
}
