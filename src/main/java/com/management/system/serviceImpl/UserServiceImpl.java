package com.management.system.serviceImpl;

import com.management.system.constants.SystemConstants;
import com.management.system.dao.UserDao;
import com.management.system.jwt.CustomerUserDetailsService;
import com.management.system.jwt.JwtFilter;
import com.management.system.jwt.JwtUtil;
import com.management.system.pojo.User;
import com.management.system.service.UserService;
import com.management.system.utils.SystemUtils;
import com.management.system.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        try{
        if (validateSignUpMap(requestMap)){
            User user = userDao.findByEmailId(requestMap.get("email"));
            if (Objects.isNull(user)){
                userDao.save(getUserFromMap(requestMap));
                return SystemUtils.getResponseEntity(SystemConstants.SUCCESSFULLY_REGISTRED,HttpStatus.OK);
            }else {
                return SystemUtils.getResponseEntity(SystemConstants.EMAIL_ALREADY_EXIST,HttpStatus.BAD_REQUEST);
            }
        }else {
            return SystemUtils.getResponseEntity(SystemConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
        }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return SystemUtils.getResponseEntity(SystemConstants.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String,String> requestMap) {
        if (requestMap.containsKey("name") && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email") && requestMap.containsKey("password")) {
            return true;
        } else {
            return false;
        }
    }

    private User getUserFromMap(Map<String,String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");

        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try{
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
            );
            if (auth.isAuthenticated()){
                if (customerUserDetailsService.getUserDetails().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String>(
                            "{\"token\":\""+jwtUtil.generateToken(
                                    customerUserDetailsService.getUserDetails().getEmail(),
                                    customerUserDetailsService.getUserDetails().getRole())+"\"}",
                                    HttpStatus.OK
                    );
                }else {
                    return new ResponseEntity<String>("{\"Message\":\""+"Wait for admin approval."+"\"}",HttpStatus.BAD_REQUEST);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<String>("{\"Message\":\""+"Bad Credentials."+"\"}",HttpStatus.BAD_REQUEST);

    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        try {
            if (jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getUsers(),HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ArrayList<>(),HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
