package com.mygoal.service;

import com.mygoal.dto.auth.AuthResponse;
import com.mygoal.dto.auth.LoginRequest;
import com.mygoal.dto.auth.RegisterRequest;
import com.mygoal.entity.User;
import com.mygoal.repository.UserRepository;
import com.mygoal.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .provider(User.AuthProvider.LOCAL)
                .build();

        userRepository.save(user);

        // Envia e-mail de boas-vindas de forma assíncrona
        mailService.sendWelcomeEmail(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // login continua igual...
}