package com.jv.ticket.user.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService detailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        if ((requestURI.equals("/api/v1/users") && method.equals("POST")) ||
                (requestURI.equals("/api/v1/auth") && method.equals("POST"))) {
            log.info("Pulando processamento JWT para rota pública: {} {}", method, requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(JwtUtils.JWT_AUTHORIZATION);

        log.info("Processando requisição: {} {}", request.getMethod(), request.getRequestURI());

        if (token == null || !token.startsWith(JwtUtils.JWT_BEARER)) {
            log.info("JWT está nulo, vazio ou não começa com Bearer. Token: {}", token);
            filterChain.doFilter(request, response);
            return;
        }

        if (!JwtUtils.isTokenValid(token)) {
            log.warn("JWT é inválido ou expirou");
            filterChain.doFilter(request, response);
            return;
        }

        String email = JwtUtils.getEmailFromToken(token);
        log.info("Email extraído do token: {}", email);
        toAuthentication(request, email);

        filterChain.doFilter(request, response);
    }

    private void toAuthentication(HttpServletRequest request, String email) {
        try {
            log.info("Tentando autenticar usuário com email: {}", email);
            UserDetails userDetails = detailsService.loadUserByUsername(email);
            log.info("UserDetails carregado: {}, authorities: {}", userDetails.getUsername(),
                    userDetails.getAuthorities());

            UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(
                    userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.info("Autenticação definida no SecurityContext com sucesso");
        } catch (Exception e) {
            log.error("Erro ao autenticar usuário: {}", e.getMessage(), e);
        }
    }
}
