package com.limdale.sponsortracker.controller;

import com.limdale.sponsortracker.model.AppUser;
import com.limdale.sponsortracker.model.RegisterRequest;
import com.limdale.sponsortracker.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AppUserRepository applicationUserRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public AppUser register(@RequestBody RegisterRequest request) {

        AppUser user = new AppUser(request.getUsername(),
                passwordEncoder.encode(request.getPassword()));

        applicationUserRepository.save(user);
        return user;
    }
}
