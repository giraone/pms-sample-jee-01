package com.giraone.samples.pmspoc1.entity.enums;

import java.util.HashMap;
import java.util.Map;

public enum EnumGender implements EnumString
{
	U("U"), M("M"), F("F"), I("I");

	private static Map<String, EnumGender> stringToEnum = new HashMap<String, EnumGender>();

	static
	{
		for (EnumGender e : EnumGender.values())
		{
			EnumGender.stringToEnum.put(e.toString(), e);
		}
	}

	// -------------------------------------------------------------------------------------------------

	private final String str;

	private EnumGender(String str)
	{
		this.str = str;
	}

	public static EnumGender fromString(String strValue)
	{
		EnumGender ret = EnumGender.stringToEnum.get(strValue);
		return ret == null ? EnumGender.U : ret;
	}

	@Override
	public String toString()
	{
		return this.str;
	}
}