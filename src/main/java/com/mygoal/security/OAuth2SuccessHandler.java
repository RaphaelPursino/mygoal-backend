package com.mygoal.oauth2;

import com.mygoal.entity.User;
import com.mygoal.repository.UserRepository;
import com.mygoal.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String avatarUrl = oAuth2User.getAttribute("picture");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .googleId(googleId)
                    .avatarUrl(avatarUrl)
                    .provider(User.AuthProvider.GOOGLE)
                    .build();
            return userRepository.save(newUser);
        });

        // Atualiza dados do Google se necessário
        if (!googleId.equals(user.getGoogleId())) {
            user.setGoogleId(googleId);
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        String redirectUrl = frontendUrl + "/auth/callback?token=" +
                URLEncoder.encode(token, StandardCharsets.UTF_8);

        log.info("OAuth2 login bem-sucedido para: {}", email);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}