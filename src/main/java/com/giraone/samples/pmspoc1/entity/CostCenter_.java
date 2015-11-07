package com.giraone.samples.pmspoc1.entity;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(CostCenter.class)
public class CostCenter_ extends AbstractEntity_
{
	public static volatile SingularAttribute<CostCenter, Long> oid;
	public static volatile SingularAttribute<CostCenter, String> identification;
	public static volatile SingularAttribute<CostCenter, String> description;
	
	public static final String SQL_NAME = "CostCenter";
	public static final String SQL_NAME_oid = "oid";
	public static final String SQL_NAME_identification = "identification";
	public static final String SQL_NAME_description = "description";
	
	public static final String DTO_NAME = "CostCenter";
	public static final String DTO_NAME_oid = "oid";
	public static final String DTO_NAME_identification = "identification";
	public static final String DTO_NAME_description = "description";
}
