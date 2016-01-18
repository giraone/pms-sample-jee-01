package com.giraone.samples.common.entity.enums;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.validation.Constraint;
import javax.validation.Payload;

// TODO: This is only a marker, that this kind of string enumeration validation is needed.
/**
 * An annotation to mark attributes of an entity as being stored as a string based data type in the
 * relational database. Usually using <i>varchar</i> or <char> SQL data type.
 * It also defines a validator: {@link StringEnumerationValidator}.
 */
@Documented
@Constraint(validatedBy = StringEnumerationValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface StringEnumeration
{	
	String message() default "{com.xxx.bean.validation.constraints.StringEnumeration.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Class<? extends Enum<?>> enumClass();
}