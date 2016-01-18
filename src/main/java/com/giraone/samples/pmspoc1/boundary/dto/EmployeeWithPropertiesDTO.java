package com.giraone.samples.pmspoc1.boundary.dto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress;

@XmlRootElement
public class EmployeeWithPropertiesDTO extends EmployeeDTO
{
	private static final long serialVersionUID = 1L;
	
	private String nationalityCode;
	private String religion;
	private int numberOfChildren;
	private Calendar dateOfEntry;
	private String maritalStatus;
	private String countryOfBirth;
	private String birthPlace;
	private String birthName;
	private String contactEmailAddress1;
	private String contactEmailAddress2;
	private String contactPhone1;
	private String contactPhone2;
	private String contactFax1;
	private String contactFax2;
	
	private List<EmployeePostalAddressDTO> postalAddresses;
	
	public EmployeeWithPropertiesDTO()
	{
		super();
	}

	public EmployeeWithPropertiesDTO(final Employee entity)
	{
		if (entity != null)
		{
			EmployeeMapper.INSTANCE.updateDtoFromEntity(entity, this);
			if (entity.getPostalAddresses() != null && entity.getPostalAddresses().size() > 0)
			{
				List<EmployeePostalAddressDTO> postalAddressesListDTO = new ArrayList<EmployeePostalAddressDTO>();
				for (EmployeePostalAddress p : entity.getPostalAddresses())
				{
					postalAddressesListDTO.add(new EmployeePostalAddressDTO(p));
				}
				this.setPostalAddresses(postalAddressesListDTO);
			}			
		}
	}

	public Employee entityFromDTO()
	{
		Employee entity = new Employee();
		EmployeeMapper.INSTANCE.updateEntityFromDto(this, entity);
		if (this.getPostalAddresses() != null && this.getPostalAddresses().size() > 0)
		{
			List<EmployeePostalAddress> postalAddressesList = new ArrayList<EmployeePostalAddress>();
			for (EmployeePostalAddressDTO p : this.getPostalAddresses())
			{
				EmployeePostalAddress employeePostalAddress = new EmployeePostalAddress();
				EmployeePostalAddressMapper.INSTANCE.updateEntityFromDto(p, employeePostalAddress);
				postalAddressesList.add(employeePostalAddress);
			}
			entity.setPostalAddresses(postalAddressesList);
		}
		return entity;
	}
	
	public Employee mergeFromDTO(Employee entity, EntityManager em)
	{
		EmployeeMapper.INSTANCE.updateEntityFromDto(this, entity);
		entity = em.merge(entity);
		return entity;
	}

	public String getNationalityCode()
	{
		return this.nationalityCode;
	}

	public void setNationalityCode(String nationalityCode)
	{
		this.nationalityCode = nationalityCode;
	}

	public Calendar getDateOfEntry()
	{
		return this.dateOfEntry;
	}

	public void setDateOfEntry(Calendar dateOfEntry)
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

	public String getReligion()
	{
		return religion;
	}

	public void setReligion(String religion)
	{
		this.religion = religion;
	}

	public int getNumberOfChildren()
	{
		return numberOfChildren;
	}

	public void setNumberOfChildren(int numberOfChildren)
	{
		this.numberOfChildren = numberOfChildren;
	}

	public String getMaritalStatus()
	{
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus)
	{
		this.maritalStatus = maritalStatus;
	}

	public String getCountryOfBirth()
	{
		return countryOfBirth;
	}

	public void setCountryOfBirth(String countryOfBirth)
	{
		this.countryOfBirth = countryOfBirth;
	}

	public String getBirthPlace()
	{
		return birthPlace;
	}

	public void setBirthPlace(String birthPlace)
	{
		this.birthPlace = birthPlace;
	}

	public String getBirthName()
	{
		return birthName;
	}

	public void setBirthName(String birthName)
	{
		this.birthName = birthName;
	}

	public String getContactEmailAddress1()
	{
		return contactEmailAddress1;
	}

	public void setContactEmailAddress1(String contactEmailAddress1)
	{
		this.contactEmailAddress1 = contactEmailAddress1;
	}

	public String getContactEmailAddress2()
	{
		return contactEmailAddress2;
	}

	public void setContactEmailAddress2(String contactEmailAddress2)
	{
		this.contactEmailAddress2 = contactEmailAddress2;
	}

	public String getContactPhone1()
	{
		return contactPhone1;
	}

	public void setContactPhone1(String contactPhone1)
	{
		this.contactPhone1 = contactPhone1;
	}

	public String getContactPhone2()
	{
		return contactPhone2;
	}

	public void setContactPhone2(String contactPhone2)
	{
		this.contactPhone2 = contactPhone2;
	}

	public String getContactFax1()
	{
		return contactFax1;
	}

	public void setContactFax1(String contactFax1)
	{
		this.contactFax1 = contactFax1;
	}

	public String getContactFax2()
	{
		return contactFax2;
	}

	public void setContactFax2(String contactFax2)
	{
		this.contactFax2 = contactFax2;
	}
}