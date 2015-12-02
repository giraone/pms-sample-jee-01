package com.giraone.samples.pmspoc1.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;

/**
 * Any postal address as an abstract base class, e.g. for employees or companies.
 * <pre>
 * Herrn                         ["to Mr." (form of address)]
 * Eberhard Wellhausen           [name]
 * Wittekindshof                 [institution / company name] 
 * Schulstrasse 4                [street address]
 * 32547 Bad Oyenhausen          [postal code + city/town]
 * GERMANY                       [country]
 * </pre>
 * Hint on postal code: The German Postal Service prefers that you no longer prefix the numeric
 * postal code with the letter(s)-based country code like D-32547!
 */
@MappedSuperclass
public abstract class PostalAddress
{
	/** a rank within multiple postal addresses of one person or company */
	@Column(name = PostalAddress_.SQL_NAME_ranking, nullable = false)
	protected int ranking;
	
	/* ISO 3166-1 Alpha-3 code */
	@Column(name = PostalAddress_.SQL_NAME_countryCode, nullable = false, length = 3)
	@Size(max = 3)
	protected String countryCode;
	
	@Column(name = PostalAddress_.SQL_NAME_postalCode, nullable = true, length = 64)
	@Size(max = 5)
	protected String postalCode;
	
	@Column(name = PostalAddress_.SQL_NAME_city, nullable = true, length = 128)
	@Size(max = 128)
	protected String city;
	
	@Column(name = PostalAddress_.SQL_NAME_secondaryAddressLine, nullable = true, length = 128)
	@Size(max = 128)
	protected String secondaryAddressLine;
	
	@Column(name = PostalAddress_.SQL_NAME_street, nullable = true, length = 128)
	@Size(max = 128)
	protected String street;
	
	@Column(name = PostalAddress_.SQL_NAME_houseNumber, nullable = true, length = 128)
	@Size(max = 20)
	protected String houseNumber;
	
	@Column(name = PostalAddress_.SQL_NAME_poBoxNumber, nullable = true, length = 128)
	@Size(max = 128)
	protected String poBoxNumber;

	
	public int getRanking()
	{
		return ranking;
	}

	public void setRanking(int ranking)
	{
		this.ranking = ranking;
	}

	public String getCountryCode()
	{
		return countryCode;
	}

	public void setCountryCode(String countryCode)
	{
		this.countryCode = countryCode;
	}

	public String getPostalCode()
	{
		return postalCode;
	}

	public void setPostalCode(String postalCode)
	{
		this.postalCode = postalCode;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getSecondaryAddressLine()
	{
		return secondaryAddressLine;
	}

	public void setSecondaryAddressLine(String secondaryAddressLine)
	{
		this.secondaryAddressLine = secondaryAddressLine;
	}

	public String getStreet()
	{
		return street;
	}

	public void setStreet(String street)
	{
		this.street = street;
	}

	public String getHouseNumber()
	{
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber)
	{
		this.houseNumber = houseNumber;
	}

	public String getPoBoxNumber()
	{
		return poBoxNumber;
	}

	public void setPoBoxNumber(String poBoxNumber)
	{
		this.poBoxNumber = poBoxNumber;
	}
}