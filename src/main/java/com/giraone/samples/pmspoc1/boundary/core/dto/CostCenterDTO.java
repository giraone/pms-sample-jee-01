package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.XmlRootElement;

import com.giraone.samples.pmspoc1.entity.CostCenter;

@XmlRootElement
public class CostCenterDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
    
	private long oid;
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

	public CostCenter entityFromDTO()
	{
		CostCenter entity = new CostCenter();
		CostCenterMapper.INSTANCE.updateEntityFromDto(this, entity);
		return entity;
	}
	
	public CostCenter mergeFromDTO(CostCenter entity, EntityManager em)
	{
		CostCenterMapper.INSTANCE.updateEntityFromDto(this, entity);
		entity = em.merge(entity);
		return entity;
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

	public void setVersionNumber(int versionNumber)
	{
		this.versionNumber = versionNumber;
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