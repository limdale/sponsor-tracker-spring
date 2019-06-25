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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private AppUserRepository appUserRepository;

    private ObjectMapper mapper = new ObjectMapper();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, AppUserRepository appUserRepository) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
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
        // TODO actual key
        SecretKey key = Keys.hmacShaKeyFor("sponsortrackerrandomkeythatismorethan256bits".getBytes()); //or HS384 or HS512
        return Jwts.builder()
                .setSubject(mapper.writeValueAsString(user))
                .signWith(key)
                .compact();
    }
}
