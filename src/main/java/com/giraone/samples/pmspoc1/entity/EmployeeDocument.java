package com.giraone.samples.pmspoc1.entity;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = EmployeeDocument_.SQL_NAME)
public class EmployeeDocument implements Serializable
{
	/** Default value included to remove warning. **/
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = EmployeeDocument_.SQL_NAME_oid)
	private long oid;
	
	@Version
	@Column(name = EmployeeDocument_.SQL_NAME_versionNumber)
	@NotNull
	private int versionNumber;

	// (1) Every employee document must have a associated employee: optional = false
	// (2) We always use FetchType.LAZY to have full control in criteria API, when we'd like to fetch more data.
	@ManyToOne(fetch = FetchType.LAZY, optional = false, targetEntity = Employee.class)
	@JoinColumn(name = EmployeeDocument_.SQL_NAME_employeeId, nullable = false)
	private Employee employee;
	
	/**
	 * The business type of the document, e.g. "employment contract", "dismissal", "ID photo"
	 */
	@Column(name = EmployeeDocument_.SQL_NAME_businessType, nullable = false, length = 128)	
	@Size(max = 128)
	private String businessType;
	
	/**
	 * The date, when this document was published.
	 */
	@Column(name = EmployeeDocument_.SQL_NAME_publishingDate, nullable = true)
	@Temporal(TemporalType.DATE)
	private Calendar publishingDate;
	
	/**
	 * The MIME type of the document (technical type).
	 * Documents without any type are classified as "application/octet-stream" 
	 */
	@Column(name = EmployeeDocument_.SQL_NAME_mimeType, nullable = false, length = 128)
	@NotNull
	@Size(max = 128)
	private String mimeType;

	@Column(name = EmployeeDocument_.SQL_NAME_bytesSize, nullable = false)	
	private long documentBytesSize;
	
	@Column(name = EmployeeDocument_.SQL_NAME_documentBytes)	
	@Basic(fetch=FetchType.LAZY)
	@Lob
	private byte[] documentBytes;
	
	public EmployeeDocument()
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

	public String getBusinessType()
	{
		return businessType;
	}

	public void setBusinessType(String businessType)
	{
		this.businessType = businessType;
	}

	public Calendar getPublishingDate()
	{
		return publishingDate;
	}

	public void setPublishingDate(Calendar publishingDate)
	{
		this.publishingDate = publishingDate;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public long getDocumentBytesSize()
	{
		return documentBytesSize;
	}

	public void setDocumentBytesSize(long documentBytesSize)
	{
		this.documentBytesSize = documentBytesSize;
	}

	public byte[] getDocumentBytes()
	{
		return documentBytes;
	}

	public void setDocumentBytes(byte[] documentBytes)
	{
		this.documentBytes = documentBytes;
	}

	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
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
