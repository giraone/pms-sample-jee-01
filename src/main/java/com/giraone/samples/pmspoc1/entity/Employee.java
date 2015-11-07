package com.giraone.samples.pmspoc1.entity;

import java.io.Serializable;
import java.util.Calendar;

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

	// Having a cost center is optional at the database level. This simplifies the overall workflow.
	@ManyToOne(fetch = FetchType.EAGER, optional = true, targetEntity = CostCenter.class, cascade = CascadeType.REFRESH)
	@JoinColumn(nullable = true)
	// @ForeignKey (nur OpenJPA)
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
}
