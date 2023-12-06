package com.management.system.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SystemUtils {

    public SystemUtils(){}

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus){
        return new ResponseEntity<>("{\"Message\":\""+responseMessage+"\"}", httpStatus);
    }
}
