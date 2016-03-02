package com.giraone.samples.pmspoc1.entity;

import java.sql.Date;
import java.util.List;

import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.giraone.samples.common.entity.AbstractEntity_;

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
	public static volatile PluralAttribute<Employee, List<EmployeePostalAddress>, EmployeePostalAddress> postalAddresses;
	
	public static final String SQL_NAME = "Employee";
	
	public static final String SQL_NAME_PROPERTIES = "EmployeeProperties";
	
	public static final String SQL_NAME_oid = "oid";
	public static final String SQL_NAME_personnelNumber = "personnelNumber";
	public static final String SQL_NAME_costCenter = "costCenter";
	public static final String SQL_NAME_lastName = "lastName";
	public static final String SQL_NAME_firstName = "firstName";
	public static final String SQL_NAME_dateOfBirth = "dateOfBirth";
	public static final String SQL_NAME_gender = "gender";
	
	/** Nationality code system plus code separated by #, e.g. "ISO-3166-1-alpha-2#DE" **/
	public static final String SQL_NAME_PROPERTY_nationalityCode = "nationalityCode";	
	public static final String SQL_NAME_PROPERTY_religion = "religion";
	public static final String SQL_NAME_PROPERTY_numberOfChildren = "numberOfChildren";
	public static final String SQL_NAME_PROPERTY_dateOfEntry = "dateOfEntry";
	public static final String SQL_NAME_PROPERTY_maritalStatus = "maritalStatus";
	/** Nationality code system plus code separated by #, e.g. "ISO-3166-3#DDDE" for DDR **/
	public static final String SQL_NAME_PROPERTY_countryOfBirth = "countryOfBirth";
	public static final String SQL_NAME_PROPERTY_birthPlace = "birthPlace";
	public static final String SQL_NAME_PROPERTY_birthName = "birthName";
	public static final String SQL_NAME_PROPERTY_contactEmailAddress1 = "contactEmailAddress1";
	public static final String SQL_NAME_PROPERTY_contactEmailAddress2 = "contactEmailAddress2";
	public static final String SQL_NAME_PROPERTY_contactPhone1 = "contactPhone1";
	public static final String SQL_NAME_PROPERTY_contactPhone2 = "contactPhone2";
	public static final String SQL_NAME_PROPERTY_contactFax1 = "contactFax1";
	public static final String SQL_NAME_PROPERTY_contactFax2 = "contactFax2";
}