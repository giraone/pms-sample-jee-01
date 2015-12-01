package com.giraone.samples.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Some commonly used string utilities.
 */
public class StringUtil
{
	private static SimpleDateFormat isoDateFormatter = new SimpleDateFormat("yyyyMMdd");
	
	public static boolean isNotNullOrEmpty(String s)
	{
		return s != null && s.length() > 0;
	}
	
	public static boolean isNotNullOrWhitespace(String s)
	{
		return s != null && s.trim().length() > 0;
	}
	
	public static String emptyIfNull(Object s)
	{
		return s != null ? s.toString() : "";
	}
	
	public static String defaultIfNull(Object s, String defaultValue)
	{
		return s != null ? s.toString() : defaultValue;
	}
		
	public static String serializeAsJavaString(String s)
	{
		if (s == null)
			return "null";
		else
			return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
	
	// Not thread safe!
	public static String formatDateAsIsoDate(Date date)
	{
		return isoDateFormatter.format(date);
	}
	
	public static String formatDateAsIsoDate(Calendar calendar)
	{
		return formatDateAsIsoDate(calendar.getTime());
	}
	
	public static GregorianCalendar parseIsoDateInput(String str) throws ParseException
	{
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(isoDateFormatter.parse(str));
		return c;
	}
	
	public static GregorianCalendar parseUserDateInput(String str)
	{
		GregorianCalendar c = new GregorianCalendar();
		String[] pieces = str.split("[.]");
		try
		{
			int day = Integer.parseInt(pieces[0], 10);
			int month = Integer.parseInt(pieces[1], 10) - 1;
			int year = Integer.parseInt(pieces[2], 10);
			if (year < 100) year += 1900;
			c.set(year, month, day);
			return c;
		}
		catch (Exception e)
		{
			return null;	
		}	
	}
}