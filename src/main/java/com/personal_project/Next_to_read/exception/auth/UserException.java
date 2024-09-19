package com.personal_project.Next_to_read.exception.auth;

sealed class UserException extends
        RuntimeException permits UserNotExistException, UserPasswordMismatchException {

    public UserException(String message) {
        super(message);
    }
}
