package com.giraone.samples.pmspoc1.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Persistence;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.giraone.samples.pmspoc1.entity.enums.EnumGender;

@Entity
@Table(name = Employee_.SQL_NAME)
// @EntityListeners(MyEntityListener.class) // DEVELOPMENT-HINT: Only for debugging JPA
public class Employee implements Serializable
{
	/** Default value included to remove warning. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = Employee_.SQL_NAME_oid)
	private Long oid;
	
	@Version
	@Column(name = Employee_.SQL_NAME_versionNumber)
	@NotNull
	private int versionNumber;
	
	@Column(name = Employee_.SQL_NAME_personnelNumber, nullable = false, length = 20, unique = true)
	@NotNull
	@Size(min = 1, max = 20)
	@Pattern(regexp = "[0-9]*", message = "Only numbers")
	private String personnelNumber;

	// (1) Having a cost center for an employee is optional at the database level, therefore
	//     we use optional=true and @JoinColumn(nullable = true). This also simplifies the overall workflow.
	// (2) We always use FetchType.LAZY to have full control in criteria API, when we'd like to fetch more data.
	// (3) We use CascadeType.REFRESH only. This means no persist/merge/remove is cascaded, Only when we call refresh()
	//     the employee will be fetched again with the cost center.
	@ManyToOne(fetch = FetchType.LAZY, optional = true, targetEntity = CostCenter.class, cascade = CascadeType.REFRESH)
	@JoinColumn(nullable = true)
	private CostCenter costCenter;

	@Column(name = Employee_.SQL_NAME_lastName, nullable = false, length = 256)
	@NotNull
	@Size(min=1, max = 256)
	private String lastName;

	@Column(name = Employee_.SQL_NAME_firstName, nullable = false, length = 256)
	@NotNull
	@Size(min=1, max = 256)
	private String firstName;

	@Enumerated(EnumType.STRING)
	@Column(name = Employee_.SQL_NAME_gender, nullable = false, length = 1)
	@NotNull
	private EnumGender gender;
	
	@Column(name = Employee_.SQL_NAME_dateOfBirth, nullable = true, length = 3)
	@Temporal(TemporalType.DATE)
	private Calendar dateOfBirth;

	/** Nationality code system plus code separated by #, e.g. "ISO-3166-1-alpha-3#DEU" **/
	@Column(name = Employee_.SQL_NAME_nationalityCode, nullable = true, length = 256)
	@Size(max = 256)
	private String nationalityCode;
	
	@Column(name = Employee_.SQL_NAME_dateOfEntry, nullable = false, length = 3)
	@Temporal(TemporalType.DATE)
	private Calendar dateOfEntry;

	// (1) We may use this list, but it is fetched lazy
	// (2) Cascade type is REMOVE and orphanRemoval = true. If the employee is removed, the postal postalAddresses are removed too
	@OneToMany(mappedBy = EmployeePostalAddress_.SQL_NAME_employee, fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.REMOVE)
	// (3) The list is order by the postal address ranking
    @OrderBy(PostalAddress_.SQL_NAME_ranking + " ASC")
	//@CascadeOnDelete  // This is an EclipseLink only annotation, which leads to ON DELETE CASCADE on the database level
	private List<EmployeePostalAddress> postalAddresses;
	
	public Employee()
	{
		super();
	}

	public Long getOid()
	{
		return oid;
	}

	public void setOid(Long oid)
	{
		this.oid = oid;
	}

	public int getVersionNumber()
	{
		return versionNumber;
	}

	public String getPersonnelNumber()
	{
		return personnelNumber;
	}

	public void setPersonnelNumber(String personnelNumber)
	{
		this.personnelNumber = personnelNumber;
	}

	public CostCenter getCostCenter()
	{
		return costCenter;
	}

	public void setCostCenter(CostCenter costCenter)
	{
		this.costCenter = costCenter;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public EnumGender getGender()
	{
		return gender;
	}

	public void setGender(EnumGender gender)
	{
		this.gender = gender;
	}

	public Calendar getDateOfBirth()
	{
		return dateOfBirth;
	}

	public void setDateOfBirth(Calendar dateOfBirth)
	{
		this.dateOfBirth = dateOfBirth;
	}

	public String getNationalityCode()
	{
		return nationalityCode;
	}

	public void setNationalityCode(String nationalityCode)
	{
		this.nationalityCode = nationalityCode;
	}

	public Calendar getDateOfEntry()
	{
		return dateOfEntry;
	}

	public void setDateOfEntry(Calendar dateOfEntry)
	{
		this.dateOfEntry = dateOfEntry;
	}

	//-- PostalAddress START ---------------------------------------------------------------

	public List<EmployeePostalAddress> getAddresses()
	{
		if (this.oid > 0 && this.postalAddresses == null && !Persistence.getPersistenceUtil().isLoaded(this.postalAddresses))
			throw new IllegalStateException("You cannot access Mitarbeiter.getAddresses when it was not loaded!");
		
		if (this.postalAddresses == null)
		{
			this.postalAddresses = new ArrayList<EmployeePostalAddress>();
		}
		return postalAddresses;
	}

	public void setAddresses(List<EmployeePostalAddress> postalAddresses)
	{
		int i = 1;
		for (EmployeePostalAddress address : postalAddresses)
		{
			address.setEmployee(this);
			address.setRanking(i++);
		}
		this.postalAddresses = postalAddresses;
	}
	
	public void addAddress(EmployeePostalAddress address)
	{
		this.getAddresses().add(address);
		address.setEmployee(this);
		address.setRanking(this.getAddresses().size());
	}
	
	public void removeAddress(EmployeePostalAddress postalAddress)
	{
		this.getAddresses().remove(postalAddress);
		int i = 1;
		for (EmployeePostalAddress address : this.getAddresses())
		{
			address.setRanking(i++);
		}
	}
	
	public void removeAddress(long postalAddressId)
	{
		EmployeePostalAddress toBeRemoved = null;
		for (EmployeePostalAddress address : this.getAddresses())
		{
			if (address.getOid() == postalAddressId)
			{
				toBeRemoved = address;
				break;
			}			
		}
		this.removeAddress(toBeRemoved);
	}
	
	//-- PostalAddress END -----------------------------------------------------------------
}
