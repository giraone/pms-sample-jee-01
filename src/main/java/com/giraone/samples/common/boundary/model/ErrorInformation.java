package com.giraone.samples.common.boundary.model;

public class ErrorInformation
{
	public static final int FIELD_VALIDATION_FAILED = 1000;
	
	int code;	
	String message;
	FieldErrorInformation[] fieldErrorInformation;
	
	public int getCode()
	{
		return code;
	}
	public void setCode(int code)
	{
		this.code = code;
	}
	public String getMessage()
	{
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
	public FieldErrorInformation[] getFieldErrorInformation()
	{
		return fieldErrorInformation;
	}
	public void setFieldErrorInformation(FieldErrorInformation[] fieldErrorInformation)
	{
		this.fieldErrorInformation = fieldErrorInformation;
	}
}
