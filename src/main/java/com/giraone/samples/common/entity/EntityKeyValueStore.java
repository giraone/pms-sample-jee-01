package com.giraone.samples.common.entity;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.giraone.samples.common.entity.enums.EnumInteger;
import com.giraone.samples.common.entity.enums.EnumString;
import com.giraone.samples.common.entity.enums.EnumValueType;
import com.giraone.samples.common.entity.enums.StringEnumeration;

/**
 * An abstract JPA entity to store properties of an entity (or resource in REST terms) not in
 * columns of the corresponding SQL table, but in key value rows of an associated key value table.
 */
@MappedSuperclass
public abstract class EntityKeyValueStore<T>
{
	public static final String DEFAULT_SQL_PARENT_NAME = "parent";
	public static final String DEFAULT_SQL_PARENT_ID_NAME = "parentId";
	
	private static final Long BOOLEAN_FALSE = new Long(0);
	private static final Long BOOLEAN_TRUE = new Long(1);
	
	public abstract void setParent(T parent);
	public abstract T getParent();

	/** Name of the key/value entry */
	@Column(name = EntityKeyValueStore_.SQL_NAME_name, nullable = false, length = 128)
	@Size(max = 64)
	protected String name;

	/** Data type of the value of the key/value entry */
	@Enumerated(EnumType.STRING)
	@StringEnumeration(enumClass = EnumValueType.class)
	@Column(name = EntityKeyValueStore_.SQL_NAME_type, nullable = false, length = 2)
	@NotNull
	protected EnumValueType type;
	
	/** Value slot for temporal data (date, time) */
	@Column(name = EntityKeyValueStore_.SQL_NAME_valueTimestamp, nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	//@Temporal(TemporalType.DATE)
	protected Date valueTimestamp;
	
	/** Value slot for numeric data (int, float) */
	@Column(name = EntityKeyValueStore_.SQL_NAME_valueNumber, nullable = true)
	protected Long valueNumber;
	
	/** Value slot for string data or any other data (other data types can be converted by a type modifier) */
	@Column(name = EntityKeyValueStore_.SQL_NAME_valueString, nullable = true, length = 256)
	@Size(max = 256)
	protected String valueString;
	
	/** An optional type modifier, to do data type conversions */
	@Column(name = EntityKeyValueStore_.SQL_NAME_typeModifier, nullable = true, length = 256)
	@Size(max = 256)
	protected String typeModifier;

	
	public EntityKeyValueStore()
	{
		super();
	}
	
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public EnumValueType getType()
	{
		return type;
	}

	public void setType(EnumValueType type)
	{
		this.type = type;
	}

	public Object getValue()
	{
		if (this.type == EnumValueType.st)
		{
			return this.getValueString();
		}
		else if (this.type == EnumValueType.dc)
		{
			return this.getValueCalendar();			
		}
		else if (this.type == EnumValueType.da)
		{
			return this.getValueTimestamp();
		}
		else if (this.type == EnumValueType.ni)
		{
			return this.getValueInteger();
		}
		else if (this.type == EnumValueType.nl)
		{
			return this.getValueLong();
		}
		else if (this.type == EnumValueType.es)
		{
			return this.getValueEnumString();
		}
		else if (this.type == EnumValueType.ei)
		{
			return this.getValueEnumInteger();
		}
		else if (this.type == EnumValueType.bo)
		{
			return this.getValueBoolean();
		}
		else
		{
			throw new IllegalArgumentException("Invalid EnumValueType " + this.type);
		}
	}
	
	public Date getValueTimestamp()
	{
		return valueTimestamp;
	}

	public void setValueTimestamp(Date valueTimestamp)
	{
		this.valueTimestamp = valueTimestamp;
	}

	public Calendar getValueCalendar()
	{
		if (this.valueTimestamp != null)
		{
			Calendar ret = new GregorianCalendar();
			ret.setTime(this.valueTimestamp);
			return ret;
		}
		return null;
	}
	
	public void setValueCalendar(Calendar valueCalendar)
	{
		this.valueTimestamp = valueCalendar != null ? valueCalendar.getTime() : null;
	}
	
	public Integer getValueInteger()
	{
		return valueNumber != null ? new Integer(valueNumber.intValue()): null;
	}

	public void setValueInteger(Integer valueNumber)
	{
		this.valueNumber = valueNumber != null ? new Long(valueNumber.longValue()): null;
	}
	
	public Long getValueLong()
	{
		return valueNumber;
	}

	public void setValueLong(Long valueNumber)
	{
		this.valueNumber = valueNumber;
	}

	public String getValueString()
	{
		return this.valueString;
	}
	
	public void setValueString(String valueString)
	{
		this.valueString = valueString;
	}
	
	@SuppressWarnings("rawtypes")
	public EnumString getValueEnumString()
	{
		try
		{
			Class clz = Class.forName(this.typeModifier);
			@SuppressWarnings("unchecked")
			Method m = clz.getMethod(EnumString.FROM_STRING_METHOD, EnumString.FROM_STRING_PARAMS);
			return (EnumString) m.invoke(null, new Object[] { this.valueString });
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Cannot create EnumString for class " + this.typeModifier + " with value \"" + this.valueString + "\"", e);
		}
	}
	
	public void setValueEnumString(EnumString valueEnumString)
	{
		this.typeModifier = valueEnumString.getClass().getCanonicalName();
		this.valueString = valueEnumString.toString();
	}
	
	@SuppressWarnings("rawtypes")
	public EnumInteger getValueEnumInteger()
	{
		try
		{
			Class clz = Class.forName(this.typeModifier);
			@SuppressWarnings("unchecked")
			Method m = clz.getMethod(EnumString.FROM_STRING_METHOD, EnumString.FROM_STRING_PARAMS);
			return (EnumInteger) m.invoke(null, new Object[] { this.valueNumber });
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Cannot create EnumInteger for class " + this.typeModifier + " with value " + this.valueNumber, e);
		}
	}
	
	public void setValueEnumInteger(EnumInteger valueEnumInteger)
	{
		this.typeModifier = valueEnumInteger.getClass().getCanonicalName();
		this.valueNumber = (long) valueEnumInteger.toInteger();
	}
	
	public boolean getValueBoolean()
	{
		return this.valueNumber > 0;
	}
	
	public void setValueBoolean(Boolean valueBoolean)
	{
		this.valueNumber = (valueBoolean != null && valueBoolean.booleanValue()) ? BOOLEAN_TRUE : BOOLEAN_FALSE;
	}
	
	public void setValueBoolean(boolean valueBoolean)
	{
		this.valueNumber = valueBoolean ? BOOLEAN_TRUE : BOOLEAN_FALSE;
	}
	
	public String getTypeModifier()
	{
		return typeModifier;
	}

	public void setTypeModifier(String typeModifier)
	{
		this.typeModifier = typeModifier;
	}
	
	public void setProperty(String key, Object value)
	{
		//System.err.println("setProperty " + key + " = \"" + value + "\" - " + ((value != null ? value.getClass() : "NULL")));
		
		this.setName(key);
		
		if (value == null)
		{
			this.setType(EnumValueType.st);
			this.setValueString(null);	
		}
		else if (value instanceof String)
		{
			this.setType(EnumValueType.st);
			this.setValueString((String) value);
		}	
		else if (value instanceof Calendar)
		{
			this.setType(EnumValueType.dc);
			this.setValueTimestamp(((Calendar) value).getTime());				
		}
		else if (value instanceof Date)
		{
			this.setType(EnumValueType.da);
			this.setValueTimestamp((Date) value);				
		}
		else if (value instanceof Integer)
		{
			this.setType(EnumValueType.ni);
			this.setValueInteger((Integer) value);
		}
		else if (value instanceof Long)
		{
			this.setType(EnumValueType.nl);
			this.setValueLong((Long) value);
		}
		else if (value instanceof EnumString)
		{
			this.setType(EnumValueType.es);
			this.setValueEnumString((EnumString) value); 
		}
		else if (value instanceof EnumInteger)
		{
			this.setType(EnumValueType.ei);
			this.setValueEnumInteger((EnumInteger) value); 
		}
		else if (value instanceof Boolean)
		{
			this.setType(EnumValueType.bo);
			this.setValueBoolean((Boolean) value); 
		}
		else
		{
			this.setType(EnumValueType.st);
			this.setValueString(value.toString());
		}			
	}
}