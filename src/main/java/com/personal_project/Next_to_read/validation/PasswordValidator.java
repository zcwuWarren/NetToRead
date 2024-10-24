package com.personal_project.Next_to_read.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        // validation rule
        return password.length() >= 8 && // at least 8 character
                password.matches(".*[A-Z].*") && // at least contain an upper case character
                password.matches(".*[a-z].*") && // at least contain a lower case character至少包含一個小寫字母
                password.matches(".*\\d.*");    // at least contain a number
    }
}
