package com.light.framework.mvc.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * @author luban
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValid.Validator.class)
public @interface EnumValid {

    String message() default "无效的枚举类型";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> value();

    String method() default "getByCode";

    class Validator implements ConstraintValidator<EnumValid, Object> {
        private EnumValid enumValid;

        @Override
        public void initialize(EnumValid enumValid) {
            this.enumValid = enumValid;
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
            if (value == null) {
                return true;
            }
            Method method = null;
            try {
                method = enumValid.value().getMethod(enumValid.method(), value.getClass());
            } catch (NoSuchMethodException e) {
                return true;
            }

            try {
                return method == null || method.invoke(null, value) != null;
            } catch (IllegalAccessException e) {
                return false;
            } catch (InvocationTargetException e) {
                return false;
            }
        }

    }
}