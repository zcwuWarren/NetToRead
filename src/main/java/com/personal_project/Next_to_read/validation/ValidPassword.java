package com.personal_project.Next_to_read.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    String message() default "密碼最少為8碼，且同時包含至少一個大寫、小寫字母和數字";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
