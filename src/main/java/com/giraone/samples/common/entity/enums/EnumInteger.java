package com.giraone.samples.common.entity.enums;

/**
 * Interface to define, that an enum is stored as an integer into the database
 */
public interface EnumInteger
{
	public static final String FROM_INTEGER_METHOD = "fromInteger";
	
	@SuppressWarnings("rawtypes")
	public static final Class[] FROM_INTEGER_PARAMS = new Class[] { Integer.class };
	
	public int toInteger();
}
