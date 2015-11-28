package com.giraone.samples.common.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

/**
 * An annotation to mark service methods as user managed transaction instead of container managed.
 * See {@link UserTransactionInterceptor} for a detailed information. 
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface UserTransactional
{
//	String handler() default ""; 
}