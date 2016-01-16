package com.giraone.samples.common.utilities.lang;

/**
 * Interface to define, that an enumeration (Java enum) is handled as an
 * <b>string value</b>, when used at non-Java-APIs, e.g. when a enumeration value is stored
 * into a database or written/read at an interface (REST, SOAP, ...).
 */
public interface EnumString
{
	public static final String FROM_STRING_METHOD = "fromString";
	
	@SuppressWarnings("rawtypes")
	public static final Class[] FROM_STRING_PARAMS = new Class[] { String.class };
}