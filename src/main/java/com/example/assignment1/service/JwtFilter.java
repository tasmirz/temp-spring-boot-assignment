package com.example.assignment1.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String reqPath = request.getRequestURI();
        System.out.println("Request: " + request.getMethod() + " " + reqPath);
        System.out.println("Authorization header: " + authHeader);
        
        // Skip JWT filter for public endpoints
        if (reqPath.startsWith("/auth/") || reqPath.equals("/") || reqPath.equals("/dashboard") || 
            reqPath.equals("/index") || reqPath.startsWith("/static/") || reqPath.startsWith("/css/") || 
            reqPath.startsWith("/js/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token extracted: " + token.substring(0, Math.min(20, token.length())) + "...");
            try {
                String username = jwtService.extractJwtData(token).username;
                String role = jwtService.extractJwtData(token).role;
                System.out.println("Username: " + username + ", Role: " + role);

                // Ensure role has ROLE_ prefix for Spring Security
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
                
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username, null,
                        Collections.singleton(() -> authority)
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("Authentication set successfully for user: " + username + " with authority: " + authority);
            } catch (Exception e) {
                System.out.println("Token parsing failed: " + e.getMessage());
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
