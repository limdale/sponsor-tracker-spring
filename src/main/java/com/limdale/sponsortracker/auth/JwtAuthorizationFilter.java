package com.limdale.sponsortracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limdale.sponsortracker.model.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
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

import static com.limdale.sponsortracker.utils.Constants.BEARER;
import static com.limdale.sponsortracker.utils.Constants.HEADER_AUTHORIZATION;
import static com.limdale.sponsortracker.utils.Constants.ROLE_PREFIX;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private ObjectMapper mapper = new ObjectMapper();

    private String secretKey;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, String jwtSecretKey) {
        super(authenticationManager);
        this.secretKey = jwtSecretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String header = request.getHeader(HEADER_AUTHORIZATION);

        if (header == null || !header.startsWith(BEARER)) {
            chain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthenticationToken(request);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthenticationToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_AUTHORIZATION);

        if (token != null) {
            String jwt = token.replace(BEARER + " ", "");
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes()); //or HS384 or HS512
            String userJson = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();

            try {
                AppUser appUser = mapper.readValue(userJson, AppUser.class);

                if (appUser != null) {
                    return new UsernamePasswordAuthenticationToken(appUser.getUsername(), null,
                            Collections.singletonList(new SimpleGrantedAuthority(ROLE_PREFIX + appUser.getRole().name())));
                }
            } catch (IOException e) {
                // TODO log exception
                return null;
            }
        }

        return null;
    }
}
