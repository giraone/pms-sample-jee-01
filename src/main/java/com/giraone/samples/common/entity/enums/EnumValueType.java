package com.giraone.samples.common.entity.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration of data types. This is the base for storing type specific properties in key values tables
 * of relational databases.
 */
public enum EnumValueType
{
	st("string"), ni("int"), nl("long"), da("date"), dc("calendar"), es("enum_string"), ei("enum_integer"), bo("boolean");

	private static Map<String, EnumValueType> stringToEnum = new HashMap<String, EnumValueType>();

	static
	{
		for (EnumValueType e : EnumValueType.values())
		{
			EnumValueType.stringToEnum.put(e.toString(), e);
		}
	}

	// -------------------------------------------------------------------------------------------------

	private final String str;

	private EnumValueType(String str)
	{
		this.str = str;
	}

	public static EnumValueType fromString(String strValue)
	{
		return EnumValueType.stringToEnum.get(strValue);
	}

	@Override
	public String toString()
	{
		return this.str;
	}
}