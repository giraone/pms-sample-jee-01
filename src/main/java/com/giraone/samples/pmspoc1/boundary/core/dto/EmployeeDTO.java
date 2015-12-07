package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import com.giraone.samples.pmspoc1.entity.Employee;

@XmlRootElement
public class EmployeeDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long oid;
	private int versionNumber;
	private String personnelNumber;
	private CostCenterDTO costCenter;
	private String lastName;
	private String firstName;
	private String gender;	
	private Calendar dateOfBirth;
	
	public EmployeeDTO()
	{
	}

	public EmployeeDTO(final Employee entity)
	{
		if (entity != null)
		{
			EmployeeMapper.INSTANCE.updateDtoFromEntity(entity, this);
		}
	}

	public Employee entityFromDTO()
	{
		Employee entity = new Employee();
		EmployeeMapper.INSTANCE.updateEntityFromDto(this, entity);
		return entity;
	}
	
	public Employee mergeFromDTO(Employee entity, EntityManager em)
	{
		EmployeeMapper.INSTANCE.updateEntityFromDto(this, entity);
		entity = em.merge(entity);
		return entity;
	}

	public long getOid()
	{
		return this.oid;
	}

	public void setOid(long oid)
	{
		this.oid = oid;
	}

	public int getVersionNumber()
	{
		return this.versionNumber;
	}

	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
	}

	public String getPersonnelNumber()
	{
		return this.personnelNumber;
	}

	public void setPersonnelNumber(String personnelNumber)
	{
		this.personnelNumber = personnelNumber;
	}

	public CostCenterDTO getCostCenter()
	{
		return this.costCenter;
	}

	public void setCostCenter(CostCenterDTO costCenter)
	{
		this.costCenter = costCenter;
	}

	public String getLastName()
	{
		return this.lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getFirstName()
	{
		return this.firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getGender()
	{
		return this.gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public Calendar getDateOfBirth()
	{
		return this.dateOfBirth;
	}

	public void setDateOfBirth(Calendar dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}
}