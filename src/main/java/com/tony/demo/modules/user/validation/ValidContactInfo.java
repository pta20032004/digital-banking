package com.tony.demo.modules.user.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
// Liên kết Annotation này với class xử lý logic bên dưới
@Constraint(validatedBy = {
        ContactInfoEntityValidator.class,
        ContactInfoDTOValidator.class })
// Chỉ định nơi dùng: FIELD (biến), METHOD (hàm)...
@Target({ ElementType.TYPE }) // Class, Interface, hoặc Enum.
// Tồn tại trong lúc chương trình đang chạy (Runtime)
@Retention(RetentionPolicy.RUNTIME)

public @interface ValidContactInfo {
    // 1. Lời nhắn báo lỗi mặc định nếu validate lỗi
    String message() default "Both Phone and Email are null";

    // 2. Groups (Bắt buộc phải có theo chuẩn của Hibernate Validator)
    Class<?>[] groups() default {};

    // 3. Payload (Bắt buộc phải có để chứa thêm metadata, thường ít xài)
    Class<? extends Payload>[] payload() default {};
}


