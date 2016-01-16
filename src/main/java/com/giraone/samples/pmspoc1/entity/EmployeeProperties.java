package com.giraone.samples.pmspoc1.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.giraone.samples.common.entity.EntityKeyValueStore;
import com.giraone.samples.common.entity.EntityKeyValueStore_;

/**
 * JPA entity to store key/value properties of employees ({@link Employee}).
 */
@Entity
@Table(name = Employee_.SQL_NAME_PROPERTIES)
public class EmployeeProperties extends EntityKeyValueStore<Employee> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	@Column(name = "id")
	protected long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Employee.class)
	@JoinColumn(name = EntityKeyValueStore_.DEFAULT_SQL_PARENT_ID_NAME, nullable = false)
	private Employee parent;
		
	public EmployeeProperties()
	{
		super();
	}
	
	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}
	
	@ManyToOne()
	public Employee getParent()
	{
		return parent;
	}

	public void setParent(Employee parent)
	{
		this.parent = parent;
	}	
}