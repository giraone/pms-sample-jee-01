package com.giraone.samples.pmspoc1.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = CostCenter_.SQL_NAME)
public class CostCenter implements Serializable
{
	/** Default value included to remove warning. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = CostCenter_.SQL_NAME_oid)
	private long oid;
	
	@Version
	@Column(name = CostCenter_.SQL_NAME_versionNumber)
	@NotNull
	private int versionNumber;
		
	@Column(name = CostCenter_.SQL_NAME_identification, nullable = false, length = 20, unique = true)
	@NotNull
	@Size(min = 1, max = 20)
	@Pattern(regexp = "[0-9A-Za-z]*", message = "Only numbers and ASCII letters are allowed")
	private String identification;
	
	@Column(name = CostCenter_.SQL_NAME_description, nullable = false, length = 256)	
	@Size(max = 256)
	private String description;

	public CostCenter()
	{
		super();
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
	
	public String getIdentification()
	{
		return identification;
	}

	public void setIdentification(String identification)
	{
		this.identification = identification;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}
}
