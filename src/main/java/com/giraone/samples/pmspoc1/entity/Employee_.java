package com.giraone.samples.pmspoc1.entity;

import java.sql.Date;

import javax.persistence.metamodel.SingularAttribute;

@javax.persistence.metamodel.StaticMetamodel(Employee.class)
public class Employee_ extends AbstractEntity_
{	
	public static volatile SingularAttribute<Employee, Long> oid;
	public static volatile SingularAttribute<Employee, String> personnelNumber;
	public static volatile SingularAttribute<Employee, CostCenter> costCenter;
	public static volatile SingularAttribute<Employee, String> lastName;
	public static volatile SingularAttribute<Employee, String> firstName;
	public static volatile SingularAttribute<Employee, Date> dateOfBirth;
	public static volatile SingularAttribute<Employee, String> gender;
	public static volatile SingularAttribute<Employee, String> nationality;
	public static volatile SingularAttribute<Employee, Date> dateOfEntry;
	
	public static final String SQL_NAME = "Employee";
	
	public static final String SQL_NAME_oid = "oid";
	public static final String SQL_NAME_personnelNumber = "personnelNumber";
	public static final String SQL_NAME_costCenter = "costCenter";
	public static final String SQL_NAME_lastName = "lastName";
	public static final String SQL_NAME_firstName = "firstName";
	public static final String SQL_NAME_dateOfBirth = "dateOfBirth";
	public static final String SQL_NAME_gender = "gender";
	
	public static final String SQL_NAME_nationalityCode = "nationalityCode";
	public static final String SQL_NAME_dateOfEntry = "dateOfEntry";
	
	public static final String DTO_NAME = "Employee";
	
	public static final String DTO_NAME_oid = "oid";
	public static final String DTO_NAME_personnelNumber = "personnelNumber";
	public static final String DTO_NAME_costCenter = "costCenter";
	public static final String DTO_NAME_lastName = "lastName";
	public static final String DTO_NAME_firstName = "firstName";
	public static final String DTO_NAME_dateOfBirth = "dateOfBirth";
	public static final String DTO_NAME_gender = "gender";
	
	public static final String DTO_NAME_nationalityCode = "nationalityCode";
	public static final String DTO_NAME_dateOfEntry = "dateOfEntry";
	
	public static final String DTO_NAME_postalAddresses = "postalAddresses";
	
	/*
	public static final String NAME_PROPERTY_familienstand = "familienstand";
	public static final String NAME_PROPERTY_geburtsland = "geburtsland";
	public static final String NAME_PROPERTY_geburtsname = "geburtsname";
	
	public static final String NAME_PROPERTY_emailAdresse = "emailAdresse";	
	public static final String NAME_PROPERTY_telefon1 = "telefon1";	
	public static final String NAME_PROPERTY_mobile1 = "mobile1";
	public static final String NAME_PROPERTY_unfallverspflichtig = "unfallverspflichtig";
	
	public static final String NAME_PROPERTY_religion = "religion";	
	public static final String NAME_PROPERTY_kinder = "kinder";
	public static final String NAME_PROPERTY_eintrittsdatum = "eintrittsdatum";
	*/
}