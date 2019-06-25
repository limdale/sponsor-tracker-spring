package com.limdale.sponsortracker.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.limdale.sponsortracker.model.LoginRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

// TODO just move the logic here to /auth/login in controller for more control
//move this to auth controller now
//it's a big hassle to inject repository here, fetch user from repo by username, serialize, then put in jwt.
//what happens rn is attemptAuthentication -> userDetailsService -> successfulAuthentication
// ideally successfulAuthentication should receive an A-ppUser object so he can serialize the user object and add it to jwt
// what we can do is db get the user in successfulAuthentication again, then serialize him
// but that makes x2 db calls on login (one for userdetailsservice by spring framework, one called manually by us)
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequest user = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
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
            // TODO actual key
            SecretKey key = Keys.hmacShaKeyFor("sponsortrackerrandomkeythatismorethan256bits".getBytes()); //or HS384 or HS512
            String token = Jwts.builder()
                    .setSubject(authResult.getName())
                    .signWith(key)
                    .compact();

            response.addHeader("Authorization", "Bearer " + token);
        }
    }
}
