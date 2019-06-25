package com.limdale.sponsortracker.auth;

import com.limdale.sponsortracker.model.AppUser;
import com.limdale.sponsortracker.model.Role;
import com.limdale.sponsortracker.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.limdale.sponsortracker.utils.Constants.ROLE_PREFIX;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    @Autowired
    private AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username);
        if (user != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name()));
            User springUser = new User(user.getUsername(), user.getPassword(), authorities);

            return springUser;
        } else {
            throw new UsernameNotFoundException(username);
        }
    }
}
