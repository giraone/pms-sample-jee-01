package com.giraone.samples.common.entity;

import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(EntityKeyValueStore.class)
public class EntityKeyValueStore_ extends AbstractEntity_
{
	// Currently not needed/used.
	// public static volatile SingularAttribute<EntityKeyValueStore, String> name;
	// public static volatile SingularAttribute<EntityKeyValueStore, String> type;
	// public static volatile SingularAttribute<EntityKeyValueStore, Date> valueTimestamp;
	
	public static final String DEFAULT_SQL_PARENT_NAME = "parent";
	public static final String DEFAULT_SQL_PARENT_ID_NAME = "parentId";
	
	public static final String SQL_NAME = "EntityKeyValueStore";
	public static final String SQL_NAME_name = "name";
	public static final String SQL_NAME_type = "type";
	public static final String SQL_NAME_valueTimestamp = "valueTimestamp";
	public static final String SQL_NAME_valueNumber = "valueNumber";
	public static final String SQL_NAME_valueString = "valueString";
	public static final String SQL_NAME_typeModifier = "typeModifier";
}