package com.handmade.controller;

import com.handmade.dto.LoginRequest;
import com.handmade.dto.LoginResponse;
import com.handmade.dto.RegisterRequest;
import com.handmade.entity.Role;
import com.handmade.entity.User;
import com.handmade.repository.UserRepository;
import com.handmade.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered");
        }

        Role role = request.getRole();

        if (role == null) {
            role = Role.CUSTOMER;
        }

        String encryptedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                request.getEmail(),
                encryptedPassword,
                role
        );

        User savedUser = userRepository.save(user);

        savedUser.setPassword(null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedUser);
    }

   @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    System.out.println("LOGIN EMAIL = [" + request.getEmail() + "]");
    System.out.println("PASSWORD RECEIVED = " + (request.getPassword() != null));

    User user = userRepository
            .findByEmail(request.getEmail())
            .orElse(null);

    System.out.println("USER FOUND = " + (user != null));

    if (user == null) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }

    boolean passwordMatches = passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
    );

    System.out.println("PASSWORD MATCHES = " + passwordMatches);

    if (!passwordMatches) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }

    String token = jwtService.generateToken(user);

    LoginResponse response = new LoginResponse(
            token,
            user.getName(),
            user.getEmail(),
            user.getRole().name()
    );

    return ResponseEntity.ok(response);
}

@PutMapping("/reset-password")
public ResponseEntity<?> resetPassword(
        @RequestParam String email,
        @RequestParam String newPassword) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    user.setPassword(passwordEncoder.encode(newPassword));

    userRepository.save(user);

    return ResponseEntity.ok("Password reset successfully");
}
}