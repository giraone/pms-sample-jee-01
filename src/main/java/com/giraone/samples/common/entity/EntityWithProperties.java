package com.giraone.samples.common.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * An abstract generic class to be used as the base class for JPA entity classes that
 * want to store some of their attributes not as columns in SQL, but in key value tables.
 * @param <T>
 */
public abstract class EntityWithProperties<T extends EntityKeyValueStore>
{
	Class<T> cls;
	
	public abstract void setParent(T properties);
	public abstract Map<String, T> getProperties();
	public abstract void setProperties(Map<String, T> properties);
	
	public EntityWithProperties(Class<T> cls)
	{
		this.cls = cls;
	}
	
	public T createProperties()
	{
		try
		{
			return (T) this.cls.newInstance();
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
			this.setParent(property);
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
	
	public void addPropertyValues(Map<String, Object> values)
	{
		for (Iterator<String> iter = values.keySet().iterator(); iter.hasNext(); )
		{
			String key = iter.next();
			Object value = values.get(key);
			this.createAndSetProperty(key, value);
		}		
	}
	
	public void setPropertyValues(Map<String, Object> values)
	{
		this.getProperties().clear();
		this.addPropertyValues(values);
	}
}