package com.system.inventorysystem.config;
import java.util.Collections;
import java.util.List;

import com.system.inventorysystem.entity.AppUser;
import com.system.inventorysystem.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;




@Component("userDetailsService")
public class UserDetailServiceImpl implements UserDetailsService {
    private final UserService userService;

    public UserDetailServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Fetch the user from your service
        AppUser user = userService.findByUserName(username);

        // 2. Add a null check to avoid NullPointerException
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        // 3. Handle the account status
        // While you can throw an exception here, Spring's 'User' object
        // can handle this via the 'enabled' boolean.
        boolean isEnabled = Boolean.TRUE.equals(user.getStatus());

        if (!isEnabled) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa");
        }

        return new User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}