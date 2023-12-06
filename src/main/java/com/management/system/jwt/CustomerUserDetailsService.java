package com.management.system.jwt;

import com.management.system.dao.UserDao;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    UserDao userDao;

    @Getter
    private com.management.system.pojo.User userDetails;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        userDetails = userDao.findByEmailId(username);
        if (!Objects.isNull(userDetails))
            return new User(userDetails.getEmail(),userDetails.getPassword(), new ArrayList<>());
        else
            throw new UsernameNotFoundException("User not found.");
    }

}
