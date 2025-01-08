package com.light.framework.mvc.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

import org.springframework.web.util.HtmlUtils;

import com.light.framework.mvc.filter.light.inner.XssFilter;

/**
 *
 * 提供方法级xss脚本注解校验，因通过SpringAOP的MethodInterceptor切面集成，<br>
 * 所以优先级小于{@link XssFilter}。
 * 需要搭配{@link com.light.core.annotation.UnEscapeHtml}，该注解才能生效
 *
 * @author luban
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Constraint(validatedBy = {Xss.XssValidator.class})
public @interface Xss {
    String message()

    default "非法输入，检测到潜在的XSS攻击";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class XssValidator implements ConstraintValidator<Xss, String> {
        private Xss xss;

        @Override
        public void initialize(Xss xss) {
            this.xss = xss;
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            return value.equals(HtmlUtils.htmlEscape(value));
        }
    }
}