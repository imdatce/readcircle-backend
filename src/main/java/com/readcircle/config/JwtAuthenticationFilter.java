package com.readcircle.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. İsteğin başlığından (Header) Authorization kısmını al
        String authHeader = request.getHeader("Authorization");

        // 2. Header var mı ve "Bearer " ile başlıyor mu kontrol et
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer " kısmını at, sadece token kalsın

            try {
                // 3. Token'ı çözümle (Validate)
                Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();

                // 4. Eğer kullanıcı adı geçerliyse, Spring Security'ye "Bu kişi giriş yaptı" de.
                if (username != null) {
                    // Not: Burada normalde veritabanından yetkileri (Rolleri) çekebiliriz.
                    // Şimdilik boş liste (new ArrayList<>()) veriyoruz.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception e) {
                // Token geçersizse veya süresi dolmuşsa buraya düşer.
                // Loglayabilirsin: System.out.println("Token hatası: " + e.getMessage());
                // Güvenlik bağlamını temizle ki giriş yapmış sayılmasın
                SecurityContextHolder.clearContext();
            }
        }

        // 5. Filtre zincirine devam et (Diğer kontrollere geç)
        filterChain.doFilter(request, response);
    }
}