package com.tony.demo.modules.user.validation;

import com.tony.demo.modules.user.domain.User;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Tham số thứ 1 là Tên Annotation 
// Tham số thứ 2 là Kiểu dữ liệu cần validate (String)
public class ContactInfoEntityValidator implements ConstraintValidator<ValidContactInfo, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user == null) { // class này không có nhiệm vụ xử lí vấn đề này
            return true;
        }

        return user.getEmail() != null || user.getPhoneNumber() != null;
    }

}
