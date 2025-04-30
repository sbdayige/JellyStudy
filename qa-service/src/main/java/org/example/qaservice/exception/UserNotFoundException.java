package org.example.qaservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {
    public UserNotFoundException(String userId) {
        super(
                HttpStatus.NOT_FOUND,
                "用户ID不存在: " + userId,
                new RuntimeException("USER_NOT_FOUND") // Root cause
        );
    }
}