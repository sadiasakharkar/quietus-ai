package com.quietusai.application.service;

import com.quietusai.api.dto.*;
import com.quietusai.config.AppProperties;
import com.quietusai.domain.entity.Role;
import com.quietusai.domain.entity.User;
import com.quietusai.domain.repository.RoleRepository;
import com.quietusai.domain.repository.UserRepository;
import com.quietusai.infrastructure.exception.ApiException;
import com.quietusai.security.jwt.JwtService;
import com.quietusai.security.service.AppUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AppProperties appProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "Email already registered");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "ROLE_NOT_FOUND", "Default role USER missing"));

        User user = new User();
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(userRole));

        User saved = userRepository.save(user);

        return new RegisterResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getRoles().stream().map(Role::getName).toList(),
                saved.getCreatedAt()
        );
    }

    public LoginResponse login(LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
        );

        AppUserPrincipal principal = (AppUserPrincipal) auth.getPrincipal();
        List<String> roles = principal.roleNames();

        String token = jwtService.generateToken(principal.getEmail(), roles);

        return new LoginResponse(
                token,
                "Bearer",
                appProperties.jwt().expirationSeconds(),
                new UserInfoResponse(principal.getUserId(), principal.getEmail(), roles)
        );
    }
}
