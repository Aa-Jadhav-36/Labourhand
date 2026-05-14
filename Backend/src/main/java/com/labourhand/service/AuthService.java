package com.labourhand.service;

import com.labourhand.dto.AuthDto;
import com.labourhand.dto.UserDto;
import com.labourhand.model.*;
import com.labourhand.repository.*;
import com.labourhand.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : User.Role.WORKER)
                .verified(false)
                .language(User.Language.valueOf(req.getLanguage() != null ? req.getLanguage() : "en"))
                .build();
        user = userRepository.save(user);

        if (user.getRole() == User.Role.WORKER) {
            WorkerProfile profile = WorkerProfile.builder()
                    .user(user)
                    .specialization(req.getSpecialization() != null ? req.getSpecialization() : "")
                    .skills(new ArrayList<>())
                    .certifications(new ArrayList<>())
                    .build();
            workerProfileRepository.save(profile);
        } else {
            OwnerProfile profile = OwnerProfile.builder()
                    .user(user)
                    .companyName(req.getCompanyName() != null ? req.getCompanyName() : "")
                    .build();
            ownerProfileRepository.save(profile);
        }

        String token = jwtUtil.generateToken(user.getEmail());
        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        response.setUser(UserService.toDto(user, workerProfileRepository, ownerProfileRepository));
        return response;
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getEmail());
        AuthDto.AuthResponse response = new AuthDto.AuthResponse();
        response.setToken(token);
        response.setUser(UserService.toDto(user, workerProfileRepository, ownerProfileRepository));
        return response;
    }
}
