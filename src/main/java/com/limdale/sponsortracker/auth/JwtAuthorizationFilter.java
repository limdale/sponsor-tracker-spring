package com.limdale.sponsortracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limdale.sponsortracker.model.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private ObjectMapper mapper = new ObjectMapper();

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthenticationToken(request);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token != null) {
            String jwt = token.replace("Bearer ", "");
            // TODO actual key
            SecretKey key = Keys.hmacShaKeyFor("sponsortrackerrandomkeythatismorethan256bits".getBytes()); //or HS384 or HS512
            String userJson = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();

            try {
                AppUser appUser = mapper.readValue(userJson, AppUser.class);

                if (appUser != null) {
                    return new UsernamePasswordAuthenticationToken(appUser.getUsername(), null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name())));
                }
            } catch (IOException e) {
                // log exception
                return null;
            }
        }

        return null;
    }
}
