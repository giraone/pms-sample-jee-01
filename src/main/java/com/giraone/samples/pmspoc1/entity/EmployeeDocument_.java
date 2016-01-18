package com.giraone.samples.pmspoc1.entity;

import java.util.Date;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

import com.giraone.samples.common.entity.AbstractEntity_;

@StaticMetamodel(EmployeeDocument.class)
public class EmployeeDocument_ extends AbstractEntity_
{
	public static volatile SingularAttribute<EmployeeDocument, Long> oid;
	public static volatile SingularAttribute<EmployeeDocument, Employee> employee;
	public static volatile SingularAttribute<EmployeeDocument, String> businessType;
	public static volatile SingularAttribute<EmployeeDocument, Date> publishingDate;	
	public static volatile SingularAttribute<EmployeeDocument, String> mimeType;
	public static volatile SingularAttribute<EmployeeDocument, Long> bytesSize;
	public static volatile SingularAttribute<EmployeeDocument, byte[]> documentBytes;
	
	public static final String SQL_NAME = "EmployeeDocument";
	public static final String SQL_NAME_oid = "oid";
	public static final String SQL_NAME_employeeId = "employeeId";	
	public static final String SQL_NAME_businessType = "businessType";
	public static final String SQL_NAME_publishingDate = "publishingDate";
	public static final String SQL_NAME_mimeType = "mimeType";
	public static final String SQL_NAME_bytesSize = "bytesSize";
	public static final String SQL_NAME_documentBytes = "documentBytes";
}