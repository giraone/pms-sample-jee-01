package com.giraone.samples.common.utilities.lang;

/**
 * Interface to define, that an enumeration (Java enum) is handled as an
 * <b>integer value</b>, when used at non-Java-APIs, e.g. when a enumeration value is stored
 * into a database or written/read at an interface (REST, SOAP, ...).
 */
public interface EnumInteger
{
	public static final String FROM_INTEGER_METHOD = "fromInteger";
	
	@SuppressWarnings("rawtypes")
	public static final Class[] FROM_INTEGER_PARAMS = new Class[] { Integer.class };
	
	public int toInteger();
}
