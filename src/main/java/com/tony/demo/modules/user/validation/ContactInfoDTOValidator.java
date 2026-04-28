package com.tony.demo.modules.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContactInfoDTOValidator implements ConstraintValidator<ValidContactInfo, String> {

    @Override
    public boolean isValid(String arg0, ConstraintValidatorContext arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isValid'");
    }
    
}
