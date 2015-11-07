package com.giraone.samples.pmspoc1.boundary.core.dto;

import java.io.Serializable;
import com.giraone.samples.pmspoc1.entity.CostCenter;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class NestedCostCenterDTO implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private Long oid;
	private int versionNumber;
	private String identification;
	private String description;

	public NestedCostCenterDTO()
	{
	}

	public NestedCostCenterDTO(final CostCenter entity)
	{
		if (entity != null)
		{
			this.oid = entity.getOid();
			this.versionNumber = entity.getVersionNumber();
			this.identification = entity.getIdentification();
			this.description = entity.getDescription();
		}
	}

	public CostCenter fromDTO(CostCenter entity, EntityManager em)
	{
		if (entity == null)
		{
			entity = new CostCenter();
		}
		if (this.oid != null)
		{
			TypedQuery<CostCenter> findByIdQuery = em
					.createQuery("SELECT DISTINCT c FROM CostCenter c WHERE c.oid = :entityId", CostCenter.class);
			findByIdQuery.setParameter("entityId", this.oid);
			try
			{
				entity = findByIdQuery.getSingleResult();
			}
			catch (javax.persistence.NoResultException nre)
			{
				entity = null;
			}
			return entity;
		}
		entity.setIdentification(this.identification);
		entity.setDescription(this.description);
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