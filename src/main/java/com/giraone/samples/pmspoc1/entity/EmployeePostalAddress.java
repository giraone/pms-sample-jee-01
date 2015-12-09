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
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = EmployeePostalAddress_.SQL_NAME)
public class EmployeePostalAddress extends PostalAddress implements Serializable
{
	/** Default value included to remove warning. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = EmployeePostalAddress_.SQL_NAME_oid)
	private long oid;
	
	@Version
	@Column(name = EmployeePostalAddress_.SQL_NAME_versionNumber)
	@NotNull
	private int versionNumber;

	// (1) Every address must have a associated employee: optional = false
	// (2) We always use FetchType.LAZY to have full control in criteria API, when we'd like to fetch more data.
	@ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Employee.class)
	@JoinColumn(name = EmployeePostalAddress_.SQL_NAME_employeeId, nullable = false)
	private Employee employee;
	
	public EmployeePostalAddress()
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

	public Employee getEmployee()
	{
		return employee;
	}

	public void setEmployee(Employee employee)
	{
		this.employee = employee;
	}
}
