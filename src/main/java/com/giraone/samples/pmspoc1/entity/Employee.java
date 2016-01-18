package com.giraone.samples.pmspoc1.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.persistence.MapKey;
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

import com.giraone.samples.common.entity.EntityKeyValueStore_;
import com.giraone.samples.common.entity.EntityWithProperties;
import com.giraone.samples.common.entity.enums.StringEnumeration;
import com.giraone.samples.pmspoc1.entity.enums.EnumGender;

/**
 * An employee in our sample application. Some details on the used object model:
 * <ul>
 * <li>Employees are assigned to 0 or 1 cost centers ({@link CostCenter}), but they exist independently
 * - there is no cascade relation.</li>
 * <li>Employees may have 0 to n postal addresses, which have an order. When an employee is
 * removed, the postal addresses are removed also.</li>
 * <li>Employees have two kinds of attributes:</li>
 *  <ul>
 *  <li>Those, that are stored in the "main" SQL table, because the attributes are primary keys, foreign keys,
 * attributes that are used in queries or joins.</li>
 *  <li>Everything else. The other attributes are stored in a key value table.
 *  </ul>
 * </ul>
 */
@Entity
@Table(name = Employee_.SQL_NAME)
// @EntityListeners(MyEntityListener.class) // DEVELOPMENT-HINT: Use this for debugging JPA
public class Employee extends EntityWithProperties<Employee, EmployeeProperties> implements Serializable
{
	/** Default value included to remove warning. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = Employee_.SQL_NAME_oid)
	private long oid;
	
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
	@StringEnumeration(enumClass = EnumGender.class)
	@Column(name = Employee_.SQL_NAME_gender, nullable = false, length = 1)
	@NotNull
	private EnumGender gender;
	
	@Column(name = Employee_.SQL_NAME_dateOfBirth, nullable = true, length = 3)
	@Temporal(TemporalType.DATE)
	private Calendar dateOfBirth;

	// (1) We may use this list, but it is fetched lazy
	// (2) Cascade type is ALL and orphanRemoval = true.
	//     Addresses may be given to create operations together with the employee data (CascadeType.PERSIST)
	//     If the employee is removed, the postal postalAddresses are removed too (CascadeType.REMOVE)
	@OneToMany(mappedBy = EmployeePostalAddress_.SQL_NAME_employee, fetch = FetchType.LAZY,
		orphanRemoval = true, cascade = CascadeType.ALL)
	// (3) The list is ordered by the postal addresses' ranking
    @OrderBy(PostalAddress_.SQL_NAME_ranking + " ASC")
	//@CascadeOnDelete  // This is an EclipseLink only annotation, which leads to ON DELETE CASCADE on the database level
	private List<EmployeePostalAddress> postalAddresses;
	
	// (1) Properties are also fetched lazy. They should not be fetched in list operations
	// (2) Cascade type is ALL and orphanRemoval = true.
	//     Properties may be given to create operations together with the employee data (CascadeType.PERSIST)
    //     If the employee is removed, the properties are removed too (CascadeType.REMOVE)
	@OneToMany(mappedBy = EntityKeyValueStore_.DEFAULT_SQL_PARENT_NAME, fetch = FetchType.LAZY,
		orphanRemoval = true, cascade = CascadeType.ALL)
	@MapKey(name = EntityKeyValueStore_.SQL_NAME_name)
	private Map<String, EmployeeProperties> properties;
	
	
	public Employee()
	{
		super(Employee.class, EmployeeProperties.class);
	}

	public long getOid()
	{
		return oid;
	}

	public void setOid(long oid)
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

	//-- Properties START -----------------------------------------------------------------
	
	// Having these getter and setter is optional, but recommended, if the DTO to Entity
	// mapping should be done generic.
	
	/** Nationality code system plus code separated by #, e.g. "ISO-3166-1-alpha-3#DEU" **/
	public String getNationalityCode()
	{		
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_nationalityCode);
	}

	public void setNationalityCode(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_nationalityCode, value);
	}
	
	public Calendar getDateOfEntry()
	{
		return this.getPropertyValueCalendar(Employee_.SQL_NAME_PROPERTY_dateOfEntry);
	}

	public void setDateOfEntry(Calendar value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_dateOfEntry, value);
	}

	public String getReligion()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_religion);
	}

	public void setReligion(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_religion, value);
	}

	public int getNumberOfChildren()
	{
		return (int) this.getPropertyValueNumber(Employee_.SQL_NAME_PROPERTY_numberOfChildren, 0);
	}

	public void setNumberOfChildren(int value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_numberOfChildren, value);
	}

	public String getMaritalStatus()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_maritalStatus);
	}

	public void setMaritalStatus(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_maritalStatus, value);
	}

	public String getCountryOfBirth()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_countryOfBirth);
	}

	public void setCountryOfBirth(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_countryOfBirth, value);
	}

	public String getBirthPlace()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_birthPlace);
	}

	public void setBirthPlace(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_birthPlace, value);
	}

	public String getBirthName()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_birthName);
	}

	public void setBirthName(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_birthName, value);
	}

	public String getContactEmailAddress1()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_contactEmailAddress1);
	}

	public void setContactEmailAddress1(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_contactEmailAddress1, value);
	}

	public String getContactEmailAddress2()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_contactEmailAddress2);
	}

	public void setContactEmailAddress2(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_contactEmailAddress2, value);
	}

	public String getContactPhone1()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_contactPhone1);
	}

	public void setContactPhone1(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_contactPhone1, value);
	}

	public String getContactPhone2()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_contactPhone2);
	}

	public void setContactPhone2(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_contactPhone2, value);
	}

	public String getContactFax1()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_contactFax1);
	}

	public void setContactFax1(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_contactFax1, value);
	}

	public String getContactFax2()
	{
		return this.getPropertyValueString(Employee_.SQL_NAME_PROPERTY_contactFax2);
	}

	public void setContactFax2(String value)
	{
		this.createAndSetProperty(Employee_.SQL_NAME_PROPERTY_contactFax2, value);
	}
	
	//-- Properties END -------------------------------------------------------------------
	
	//-- EmployeeProperties START ---------------------------------------------------------

	@Override
	public void defineParentInProperty(EmployeeProperties property)
	{
		property.setParent(this);
	}
	
	@Override
	public Map<String, EmployeeProperties> getProperties()
	{		
		if (this.oid == 0L && this.properties == null)
		{
			this.properties = new HashMap<String, EmployeeProperties>();
		}
		return this.properties;
	}

	@Override
	public void setProperties(Map<String, EmployeeProperties> properties)
	{
		for (EmployeeProperties property : properties.values())
		{
			property.setParent(this);
		}
		this.properties = properties;
	}
		
	//-- EmployeeProperties END ------------------------------------------------------------
	
	
	//-- PostalAddress START ---------------------------------------------------------------

	public List<EmployeePostalAddress> getPostalAddresses()
	{
		if (this.oid > 0 && this.postalAddresses == null && !Persistence.getPersistenceUtil().isLoaded(this.postalAddresses))
			throw new IllegalStateException("You cannot access Mitarbeiter.getAddresses when it was not loaded!");
		
		if (this.postalAddresses == null)
		{
			this.postalAddresses = new ArrayList<EmployeePostalAddress>();
		}
		return postalAddresses;
	}

	public void setPostalAddresses(List<EmployeePostalAddress> postalAddresses)
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
		this.getPostalAddresses().add(address);
		address.setEmployee(this);
		address.setRanking(this.getPostalAddresses().size());
	}
	
	public void removeAddress(EmployeePostalAddress postalAddress)
	{
		this.getPostalAddresses().remove(postalAddress);
		int i = 1;
		for (EmployeePostalAddress address : this.getPostalAddresses())
		{
			address.setRanking(i++);
		}
	}
	
	public void removeAddress(long postalAddressId)
	{
		EmployeePostalAddress toBeRemoved = null;
		for (EmployeePostalAddress address : this.getPostalAddresses())
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
