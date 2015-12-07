package com.giraone.samples.common.entity.enums;

import java.lang.annotation.Documented;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * An annotation to mark attributes of an entity as being stored as a string based data type in the
 * relational database. Usually using <i>varchar</i> or <char> SQL data type.
 * It also defines a validator: {@link StringEnumerationValidator}.
 */
@Documented
@Constraint(validatedBy = StringEnumerationValidator.class)
public @interface StringEnumeration
{
	String message() default "{com.xxx.bean.validation.constraints.StringEnumeration.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Class<? extends Enum<?>> enumClass();
}