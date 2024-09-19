package com.personal_project.Next_to_read.exception;

import com.personal_project.Next_to_read.exception.auth.UserNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

//    @ExceptionHandler(UserNotExistException.class)
//    public ResponseEntity<Map<String, Object>> handleUserNotExistException(UserNotExistException e) {
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error", "User Not Exist");
//        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//    }
}

