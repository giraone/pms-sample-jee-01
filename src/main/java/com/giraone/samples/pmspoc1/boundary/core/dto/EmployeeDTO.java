package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import com.giraone.samples.pmspoc1.entity.Employee;

@XmlRootElement
public class EmployeeDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Long oid;
	private int versionNumber;
	private String personnelNumber;
	private CostCenterDTO costCenter;
	private String lastName;
	private String firstName;
	private String gender;
	private Calendar dateOfBirth;
	private String nationalityCode;
	private Calendar dateOfEntry;

	private List<EmployeePostalAddressDTO> postalAddresses;
	
	public EmployeeDTO()
	{
	}

	public EmployeeDTO(final Employee entity)
	{
		if (entity != null)
		{
			EmployeeMapper.INSTANCE.updateDtoFromEntity(entity, this);
			//this.costCenter = new NestedCostCenterDTO(entity.getCostCenter());
		}
	}

	public Employee fromDTO(Employee entity, EntityManager em)
	{
		if (entity == null)
		{
			entity = new Employee();
		}
		// entity.setCostCenter(this.costCenter.fromDTO(entity.getCostCenter(), em));
		EmployeeMapper.INSTANCE.updateEntityFromDto(this, entity);
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

	public CostCenterDTO getCostCenter()
	{
		return this.costCenter;
	}

	public void setCostCenter(final CostCenterDTO costCenter)
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
	
	public List<EmployeePostalAddressDTO> getPostalAddresses()
	{
		return postalAddresses;
	}

	public void setPostalAddresses(List<EmployeePostalAddressDTO> postalAddresses)
	{
		this.postalAddresses = postalAddresses;
	}
}