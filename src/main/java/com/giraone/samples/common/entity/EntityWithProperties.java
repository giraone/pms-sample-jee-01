package com.giraone.samples.common.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * An abstract generic class to be used as the base class for JPA entity classes that
 * want to store some of their attributes not as columns in SQL, but in key value tables.
 * @param <S>	The JPA parent class that wants to store key value attributes.
 * @param <T>	The JPA key value store class, where the attributes are stored.
 */
public abstract class EntityWithProperties<S, T extends EntityKeyValueStore<S>>
{	
	public static final String JAVA_PROPERTIES_MAP_NAME = "properties";
	
	Class<S> parentClass;
	Class<T> keyValueClass;
	
	public abstract void defineParentInProperty(T property);
	public abstract Map<String, T> getProperties();
	public abstract void setProperties(Map<String, T> properties);
	
	public EntityWithProperties(Class<S> parentClass, Class<T> keyValueClass)
	{
		this.keyValueClass = keyValueClass;
	}
	
	public T createProperties()
	{
		try
		{
			return (T) this.keyValueClass.newInstance();
		}
		catch (Exception e)
		{			
			e.printStackTrace();
			throw new IllegalStateException(e);
		}	
	}
	
	public T createAndSetProperty(String key, Object value)
	{				
		Map<String, T> properties = this.getProperties();
		if (properties == null)
		{
			throw new IllegalStateException("getProperties() of " + this + " returned null!");
		}
		
		T property;
		if ((property = properties.get(key)) == null)
		{			
			property = this.createProperties();			
			this.defineParentInProperty(property);
			properties.put(key, property);
			//System.err.println("createAndSetProperty create " + key + " " + property.type + " " + value);
		}
		else
		{
			//System.err.println("createAndSetProperty exist  " + key + " " + property.type + " " + value);
			if (value == null)
			{
				// Remove it!
				properties.remove(key);
			}
		}
		property.setProperty(key, value);
		
		return property;
	}
	
	public String getPropertyValueString(String key)
	{
		if (this.getProperties() != null)
		{
			T p = this.getProperties().get(key);
			if (p != null)
			{
				return p.getValueString();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
		
	public Long getPropertyValueNumber(String key)
	{
		if (this.getProperties() != null)
		{
			T p = this.getProperties().get(key);
			if (p != null)
			{
				return p.getValueLong();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	public long getPropertyValueNumber(String key, long defaultValue)
	{
		final Long ret = this.getPropertyValueNumber(key);
		return ret == null ? defaultValue : ret.longValue();
	}
	
	public boolean getPropertyValueBoolean(String key)
	{
		if (this.getProperties() != null)
		{
			T p = this.getProperties().get(key);
			if (p != null)
			{
				return p.getValueBoolean();
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	public Date getPropertyValueTimestamp(String key)
	{
		if (this.getProperties() != null)
		{
			T p = this.getProperties().get(key);
			if (p != null)
			{
				return p.getValueTimestamp();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	public Calendar getPropertyValueCalendar(String key)
	{
		if (this.getProperties() != null)
		{
			T p = this.getProperties().get(key);
			if (p != null)
			{
				return p.getValueCalendar();
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}
	
	public void addPropertiesFromMap(Map<String, Object> values)
	{
		for (Iterator<String> iter = values.keySet().iterator(); iter.hasNext(); )
		{
			String key = iter.next();
			Object value = values.get(key);
			this.createAndSetProperty(key, value);
		}		
	}
	
	public void replacePropertiesFromMap(Map<String, Object> values)
	{
		this.getProperties().clear();
		this.addPropertiesFromMap(values);
	}
}