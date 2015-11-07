package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;

import com.giraone.samples.pmspoc1.boundary.core.dto.NestedCostCenterDTO;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.enums.EnumGender;

import javax.persistence.EntityManager;

import java.util.Calendar;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EmployeeDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Long oid;
	private int versionNumber;
	private String personnelNumber;
	private NestedCostCenterDTO costCenter;
	private String lastName;
	private String firstName;
	private String gender;
	private Calendar dateOfBirth;
	private String nationalityCode;
	private Calendar dateOfEntry;

	public EmployeeDTO()
	{
	}

	public EmployeeDTO(final Employee entity)
	{
		if (entity != null)
		{
			this.oid = entity.getOid();
			this.versionNumber = entity.getVersionNumber();
			this.personnelNumber = entity.getPersonnelNumber();
			this.costCenter = new NestedCostCenterDTO(entity.getCostCenter());
			this.lastName = entity.getLastName();
			this.firstName = entity.getFirstName();
			this.gender = entity.getGender().toString();
			this.dateOfBirth = entity.getDateOfBirth();
			this.nationalityCode = entity.getNationalityCode();
			this.dateOfEntry = entity.getDateOfEntry();
		}
	}

	public Employee fromDTO(Employee entity, EntityManager em)
	{
		if (entity == null)
		{
			entity = new Employee();
		}
		entity.setPersonnelNumber(this.personnelNumber);
		if (this.costCenter != null)
		{
			entity.setCostCenter(this.costCenter.fromDTO(entity.getCostCenter(), em));
		}
		entity.setLastName(this.lastName);
		entity.setFirstName(this.firstName);
		entity.setGender(EnumGender.fromString(this.gender));
		entity.setDateOfBirth(this.dateOfBirth);
		entity.setNationalityCode(this.nationalityCode);
		entity.setDateOfEntry(this.dateOfEntry);
		entity = em.merge(entity);
		return entity;
	}

	public Long getOid()
	{
		return this.oid;
	}

	public void setOid(final Long oid)
	{
		this.oid = oid;
	}

	public int getVersionNumber()
	{
		return this.versionNumber;
	}

	public void setVersionNumber(final int versionNumber)
	{
		this.versionNumber = versionNumber;
	}

	public String getPersonnelNumber()
	{
		return this.personnelNumber;
	}

	public void setPersonnelNumber(final String personnelNumber)
	{
		this.personnelNumber = personnelNumber;
	}

	public NestedCostCenterDTO getCostCenter()
	{
		return this.costCenter;
	}

	public void setCostCenter(final NestedCostCenterDTO costCenter)
	{
		this.costCenter = costCenter;
	}

	public String getLastName()
	{
		return this.lastName;
	}

	public void setLastName(final String lastName)
	{
		this.lastName = lastName;
	}

	public String getFirstName()
	{
		return this.firstName;
	}

	public void setFirstName(final String firstName)
	{
		this.firstName = firstName;
	}

	public String getGender()
	{
		return this.gender;
	}

	public void setGender(final String gender)
	{
		this.gender = gender;
	}

	public Calendar getDateOfBirth()
	{
		return this.dateOfBirth;
	}

	public void setDateOfBirth(final Calendar dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	public String getNationalityCode()
	{
		return this.nationalityCode;
	}

	public void setNationalityCode(final String nationalityCode)
	{
		this.nationalityCode = nationalityCode;
	}

	public Calendar getDateOfEntry()
	{
		return this.dateOfEntry;
	}

	public void setDateOfEntry(final Calendar dateOfEntry)
	{
		this.dateOfEntry = dateOfEntry;
	}
}