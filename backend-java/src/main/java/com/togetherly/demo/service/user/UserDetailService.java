package com.togetherly.demo.service.user;

import com.togetherly.demo.data.auth.UserDetail;
import com.togetherly.demo.model.auth.User;
import com.togetherly.demo.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridge between Spring Security and our User repository.
 * Spring Security calls loadUserByUsername() during password authentication.
 */
@Service
public class UserDetailService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("user does not exist !"));
        return new UserDetail(user);
    }
}
