package com.carrental.bookingservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader("Authorization");

        System.out.println("\n================== JWT FILTER ==================");
        System.out.println("➡️ " + method + " " + path);
        System.out.println("Authorization header: " + authHeader);
        System.out.println("================================================");

        // ✅ If no token, continue (but controller method security may fail)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ No Bearer token found, continuing filter...");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("✅ Token received (first 15 chars): " + token.substring(0, Math.min(15, token.length())) + "...");

        try {
            boolean valid = jwtUtil.validateToken(token);
            System.out.println("✅ validateToken() = " + valid);

            if (!valid) {
                System.out.println("❌ TOKEN INVALID -> continuing request (may fail @PreAuthorize)");
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            System.out.println("✅ Extracted email: " + email);
            System.out.println("✅ Extracted role: " + role);

            List<SimpleGrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            System.out.println("✅ SecurityContext SET with authorities: " + authorities);
            System.out.println("================================================\n");

        } catch (Exception e) {
            System.out.println("❌ EXCEPTION IN FILTER: " + e.getClass().getName() + " => " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
