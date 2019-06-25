package com.limdale.sponsortracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limdale.sponsortracker.model.AppUser;
import com.limdale.sponsortracker.model.LoginRequest;
import com.limdale.sponsortracker.model.TokenResponse;
import com.limdale.sponsortracker.repository.AppUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

//when /login gets called, attemptAuthentication -> userDetailsService -> successfulAuthentication
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private AppUserRepository appUserRepository;
    private String secretKey;

    private ObjectMapper mapper = new ObjectMapper();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, AppUserRepository appUserRepository,
                                   String jwtSecretKey) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.secretKey = jwtSecretKey;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest user = mapper.readValue(request.getInputStream(), LoginRequest.class);
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword(),
                    new ArrayList<>()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        if (authResult.isAuthenticated()) {
            AppUser user = appUserRepository.findByUsername(authResult.getName());

            if (user != null) {
                String jwt = createJwtFromUser(user);
                String jsonResponse = mapper.writeValueAsString(new TokenResponse(user, jwt));
                response.getWriter().write(jsonResponse);
            } else {
                throw new BadCredentialsException("Invalid username/password");
            }
        } else {
            throw new BadCredentialsException("Invalid username/password");
        }
    }

    private String createJwtFromUser(AppUser user) throws IOException {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes()); //or HS384 or HS512
        return Jwts.builder()
                .setSubject(mapper.writeValueAsString(user))
                .signWith(key)
                .compact();
    }
}
