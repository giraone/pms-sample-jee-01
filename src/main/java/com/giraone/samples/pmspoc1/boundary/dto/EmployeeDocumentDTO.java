package com.giraone.samples.pmspoc1.boundary.dto;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlRootElement;

import com.giraone.samples.pmspoc1.entity.EmployeeDocument;

@XmlRootElement
public class EmployeeDocumentDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	protected long oid;
	protected int versionNumber;
	protected Long employeeId;
	protected EmployeeDTO employee;
	protected String businessType;
	protected Calendar publishingDate;
	protected String mimeType;
	protected long byteSize;
	protected byte[] bytes;
	
	public EmployeeDocumentDTO()
	{
		this.oid = 0L;				// A value of 0L indicates: not from the database!
		this.versionNumber = -1;	// A value of -1 indicates: not from the database!
	}
	
	public EmployeeDocumentDTO(final EmployeeDocument entity)
	{
		this.oid = entity.getOid();
		this.versionNumber = entity.getVersionNumber();
		this.employeeId = entity.getEmployee() != null ? entity.getEmployee().getOid() : 0;
		this.employee = entity.getEmployee() != null ? new EmployeeDTO(entity.getEmployee()) : null;
		this.businessType = entity.getBusinessType();
		this.publishingDate = entity.getPublishingDate();
		this.mimeType = entity.getMimeType();
		this.byteSize = entity.getByteSize();
	}
	
	public EmployeeDocument entityFromDTO()
	{
		EmployeeDocument ret = new EmployeeDocument();
		ret.setVersionNumber(this.versionNumber);
		ret.setEmployee(this.employee != null ? this.employee.entityFromDTO() : null);
		ret.setBusinessType(this.businessType);
		ret.setPublishingDate(this.publishingDate);
		ret.setMimeType(this.mimeType);
		ret.setByteSize(this.byteSize);
		return ret;
	}
	
	public long getOid()
	{
		return this.oid;
	}

	public void setOid(long oid)
	{
		this.oid = oid;
	}

	public int getVersionNumber()
	{
		return this.versionNumber;
	}

	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
	}
	
	public Long getEmployeeId()
	{
		return employeeId;
	}

	public void setEmployeeId(Long employeeId)
	{
		this.employeeId = employeeId;
	}

	public EmployeeDTO getEmployee()
	{
		return employee;
	}

	public void setEmployee(EmployeeDTO employee)
	{
		this.employee = employee;
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

	public long getByteSize()
	{
		return byteSize;
	}

	public void setByteSize(long byteSize)
	{
		this.byteSize = byteSize;
	}

	public byte[] getBytes()
	{
		return bytes;
	}

	public void setBytes(byte[] bytes)
	{
		this.bytes = bytes;
	}
}