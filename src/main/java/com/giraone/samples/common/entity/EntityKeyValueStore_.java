package com.giraone.samples.common.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import javax.validation.constraints.Size;

import com.giraone.samples.pmspoc1.entity.AbstractEntity_;

@StaticMetamodel(EntityKeyValueStore.class)
public class EntityKeyValueStore_ extends AbstractEntity_
{
	public static volatile SingularAttribute<EntityKeyValueStore, Long> oid;
	public static volatile SingularAttribute<EntityKeyValueStore, String> identification;
	public static volatile SingularAttribute<EntityKeyValueStore, String> description;
	
	public static final String SQL_NAME = "EntityKeyValueStore";
	public static final String SQL_NAME_name = "name";
	public static final String SQL_NAME_type = "type";
	public static final String SQL_NAME_valueTimestamp = "valueTimestamp";
	public static final String SQL_NAME_valueNumber = "valueNumber";
	public static final String SQL_NAME_valueString = "valueString";
	public static final String SQL_NAME_typeModifier = "typeModifier";
}