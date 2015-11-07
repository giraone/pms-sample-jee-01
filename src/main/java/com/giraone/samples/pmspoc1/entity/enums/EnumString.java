package com.giraone.samples.pmspoc1.entity.enums;

/**
 * Interface to define, that an enum is stored as a string into the database
 */
public interface EnumString
{
	public static final String FROM_STRING_METHOD = "fromString";
	
	@SuppressWarnings("rawtypes")
	public static final Class[] FROM_STRING_PARAMS = new Class[] { String.class };
}
