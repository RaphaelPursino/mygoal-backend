package com.mygoal.security;

import com.mygoal.entity.User;
import com.mygoal.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        log.debug("Authorization header: {}", header != null ? "presente" : "ausente");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.isValid(token)) {
            log.warn("Token inválido recebido");
            chain.doFilter(request, response);
            return;
        }

        try {
            String email = jwtService.extractEmail(token);
            log.debug("Email extraído do token: {}", email);

            User user = userRepository.findByEmail(email).orElse(null);
            log.debug("Usuário encontrado no banco: {}", user != null ? user.getEmail() : "NÃO ENCONTRADO");

            if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Autenticação definida para: {}", email);
            }
        } catch (Exception e) {
            log.error("Erro ao autenticar via JWT: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }
}