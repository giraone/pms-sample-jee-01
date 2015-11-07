package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;
import java.util.Calendar;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EmployeeSummaryDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private long count;
	private Calendar lastUpdate;

	public EmployeeSummaryDTO()
	{
	}

	public EmployeeSummaryDTO(final long count, final Calendar lastUpdate)
	{
		this.count = count;
		this.lastUpdate = lastUpdate;
	}

	public long getCount()
	{
		return count;
	}

	public void setCount(long count)
	{
		this.count = count;
	}

	public Calendar getLastUpdate()
	{
		return lastUpdate;
	}

	public void setLastUpdate(Calendar lastUpdate)
	{
		this.lastUpdate = lastUpdate;
	}
}