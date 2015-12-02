package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import com.giraone.samples.pmspoc1.entity.CostCenter;

@XmlRootElement
public class CostCenterDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
    
	private Long oid;
	private int versionNumber;
	private String identification;
	private String description;

	public CostCenterDTO()
	{
	}

	public CostCenterDTO(final CostCenter entity)
	{
		if (entity != null)
		{			
			CostCenterMapper.INSTANCE.updateDtoFromEntity(entity, this);
		}
	}

	public CostCenter fromDTO(CostCenter entity, EntityManager em)
	{
		if (entity == null)
		{
			entity = new CostCenter();
		}
		CostCenterMapper.INSTANCE.updateEntityFromDto(this, entity);
		entity = em.merge(entity);
		return entity;
	}

	public Long getOid()
	{
		return this.oid;
	}

	public void setOid(final Long oid)
	{
		this.oid = oid;
	}

	public int getVersionNumber()
	{
		return this.versionNumber;
	}

	public void setVersionNumber(final int versionNumber)
	{
		this.versionNumber = versionNumber;
	}

	public String getIdentification()
	{
		return this.identification;
	}

	public void setIdentification(final String identification)
	{
		this.identification = identification;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}
}