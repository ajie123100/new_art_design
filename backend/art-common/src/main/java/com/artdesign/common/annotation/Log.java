package com.artdesign.common.annotation;

import com.artdesign.common.enums.BusinessType;
import com.artdesign.common.enums.OperatorType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String title() default "";

    BusinessType businessType() default BusinessType.OTHER;

    OperatorType operatorType() default OperatorType.MANAGE;

    boolean saveRequestData() default true;

    boolean saveResponseData() default true;
}
