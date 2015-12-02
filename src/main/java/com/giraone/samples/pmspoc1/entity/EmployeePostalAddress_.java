package com.giraone.samples.pmspoc1.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(EmployeePostalAddress.class)
public class EmployeePostalAddress_ extends PostalAddress_
{
	public static volatile SingularAttribute<PostalAddress, Long> oid;
	
	public static final String SQL_NAME = "PostalAddress";
	public static final String SQL_NAME_employee = "employee";
	public static final String SQL_NAME_employeeId = "employeeId";
	
	public static final String DTO_NAME = "PostalAddress";
	public static final String DTO_NAME_employeeId = "employeeId";
}