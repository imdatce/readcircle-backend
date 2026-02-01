package com.readcircle.controller;

import com.readcircle.model.User;
import com.readcircle.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

     private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Bu kullanıcı adı zaten alınmış.");
        }
        userRepository.save(user);
        return ResponseEntity.ok("Kayıt başarılı");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(loginRequest.getPassword())) {
             String token = Jwts.builder()
                    .setSubject(userOpt.get().getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                    .signWith(SECRET_KEY)
                    .compact();

            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("username", userOpt.get().getUsername());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Hatalı kullanıcı adı veya şifre");
    }
}