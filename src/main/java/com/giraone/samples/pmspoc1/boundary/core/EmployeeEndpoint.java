package com.giraone.samples.pmspoc1.boundary.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.ws.rs.core.UriBuilder;

import com.giraone.samples.pmspoc1.boundary.PmsCoreApi;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeSummaryDTO;
import com.giraone.samples.pmspoc1.control.PersistenceUtil;
import com.giraone.samples.pmspoc1.control.TransactionUtil;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.Employee_;

/**
 * REST end point for CRUD operations on "employee" entities.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("/employees")
public class EmployeeEndpoint extends BaseEndpoint
{
	private static final Marker LOG_TAG = MarkerManager.getMarker("API");
	
	@Inject
	private Logger logger;
	
	@Resource
	private UserTransaction tx;

	@PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
	private EntityManager em;

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") Long id)
	{
		if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "findById; id=" + id);
		
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		final CriteriaQuery<Employee> select = c.select(table);
		final Predicate predicate = cb.equal(table.get(Employee_.oid), id);
		// Open issue: Does it make any sense to use also .distinct() here?
		select.where(predicate);
		final TypedQuery<Employee> tq = em.createQuery(select);

		final Employee entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
			Employee_.SQL_NAME_oid);
		if (entity != null)
		{
			EmployeeDTO dto = new EmployeeDTO(entity);
			return Response.ok(dto).build();
		}
		else
		{
			return Response.status(Status.NOT_FOUND).build();
		}
	}

    /**
     * Additional GET method using the also unique "personnelNumber" attribute prefixed with "pnr-"
     * 
     * @param personnelNumber
     * @return EmployeeDTO object
     */
    @GET
    @Path("/pnr-{personnelNumber:[0-9a-zA-Z][0-9a-zA-Z]*}")
    @Produces("application/json")
    public Response findByPersonnelNumber(@PathParam("personnelNumber") String personnelNumber)
    {
    	if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "findByPersonnelNumber; personnelNumber=" + personnelNumber);
    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        final Root<Employee> table = c.from(Employee.class);
        final CriteriaQuery<Employee> select = c.select(table);
        final Predicate predicate = cb.equal(table.get(Employee_.personnelNumber), personnelNumber);
        // Open issue: Does it make any sense to use also .distinct() here?
        select.where(predicate);
        final TypedQuery<Employee> tq = em.createQuery(select);

        final Employee entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
        	Employee_.SQL_NAME_personnelNumber);
        if (entity != null)
        {
        	EmployeeDTO dto = new EmployeeDTO(entity);
            return Response.ok(dto).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
    
    // TODO: Remove
	@GET
	@Path("/alternative/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById2(@PathParam("id") Long id)
	{
		TypedQuery<Employee> findByIdQuery = em.createQuery(
			"SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.costCenter WHERE e.oid = :entityId ORDER BY e.oid",
			Employee.class);
		findByIdQuery.setParameter("entityId", id);
		Employee entity;
		try
		{
			entity = findByIdQuery.getSingleResult();
		}
		catch (NoResultException nre)
		{
			entity = null;
		}
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		EmployeeDTO dto = new EmployeeDTO(entity);
		return Response.ok(dto).build();
	}

    /**
     * List all entities with support for OData filters.
     * @param filter	OData filter expression
     * @param orderby	OData sort expression
     * @param skip		OData paging
     * @param top		OData filter expression
     * @return
     */
	@GET
	@Produces("application/json")
	public List<EmployeeDTO> listAll(
		@QueryParam("filter") @DefaultValue("") String filter,
		@QueryParam("orderby") @DefaultValue("") String orderby,
		@QueryParam("skip") @DefaultValue("0") int skip,
		@QueryParam("top") @DefaultValue(DEFAULT_PAGING_SIZE) int top)
	{
		if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "listAll; filter=\"" + filter + "\", orderby=\"" + orderby + "\""
				+ ", skip=" + skip + ", top=" + top);
		
		// TypedQuery<Employee> findAllQuery = em.createQuery(
		// "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.costCenter ORDER
		// BY e.oid", Employee.class);

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		final CriteriaQuery<Employee> select = c.select(table);
		/*
        ODataToJpaQueryBuilder<Employee> filterBuilder = new ODataToJpaQueryBuilder<Employee>();
		filterBuilder.setCriteriaTable(cb, table);
		Predicate predicate = filterBuilder.parseFilterExpression(filter);
		if (predicate != null) { select.where(predicate); }
		filterBuilder.parseOrderExpression(cb, select, orderby);
		*/
        final TypedQuery<Employee> tq = em.createQuery(select);

        tq.setFirstResult(skip);
        tq.setMaxResults(top);
        
		final List<Employee> searchResults = tq.getResultList();
		final List<EmployeeDTO> results = new ArrayList<EmployeeDTO>();
		for (Employee searchResult : searchResults)
		{
			EmployeeDTO dto = new EmployeeDTO(searchResult);
			results.add(dto);
		}
		return results;
	}

	// TODO: Remove this JPQL version
	@GET
	@Path("/alternative")
	@Produces("application/json")
	public List<EmployeeDTO> listAll2(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
	{
		TypedQuery<Employee> findAllQuery = em.createQuery(
			"SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.costCenter ORDER BY e.oid", Employee.class);
		if (startPosition != null)
		{
			findAllQuery.setFirstResult(startPosition);
		}
		if (maxResult != null)
		{
			findAllQuery.setMaxResults(maxResult);
		}
		final List<Employee> searchResults = findAllQuery.getResultList();
		final List<EmployeeDTO> results = new ArrayList<EmployeeDTO>();
		for (Employee searchResult : searchResults)
		{
			EmployeeDTO dto = new EmployeeDTO(searchResult);
			results.add(dto);
		}
		return results;
	}

	@POST
	@Consumes("application/json")
	public Response create(EmployeeDTO dto)
	{
    	if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "create: personnelNumber=\""
				+ (dto == null ? "null" :dto.getPersonnelNumber()) + "\"");
    	
		final Employee entity;
		try
		{
			tx.begin();
			entity = dto.fromDTO(null, em);
			em.persist(entity);
			tx.commit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			TransactionUtil.rollback(tx);
			boolean isConstraintViolated = PersistenceUtil.isConstraintViolation(e);
			if (isConstraintViolated)
			{
				return Response.status(Status.CONFLICT).build();
			}
			throw new EJBException(e);
		}

		return Response
			.created(UriBuilder.fromResource(EmployeeEndpoint.class).path(String.valueOf(entity.getOid())).build())
			.build();
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	public Response update(@PathParam("id") Long id, EmployeeDTO dto)
	{
    	if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "update; id=" + id
				+ ", new.id" + (dto == null ? "null" : dto.getOid())
				+ ", new.personnelNumber" + (dto == null ? "null" :dto.getPersonnelNumber()) + "\"");
    	
		if (dto == null || id == null)
		{
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (!id.equals(dto.getOid()))
		{
			logger.warn(LOG_TAG, "update CONFLICT id1=" + id + ", id2=" + dto.getOid());
			return Response.status(Status.CONFLICT).entity(dto).build();
		}

		try
		{
			tx.begin();
			Employee entity = em.find(Employee.class, id);
			if (entity == null)
			{
				tx.commit();
				return Response.status(Status.NOT_FOUND).build();
			}
			entity = dto.fromDTO(entity, em);
			try
			{
				entity = em.merge(entity);
			}
			catch (OptimisticLockException e)
			{
				logger.warn(LOG_TAG, "update OptimisticLockException");
				TransactionUtil.rollback(tx);
				return Response.status(Status.CONFLICT).entity(e.getEntity()).build();
			}
			tx.commit();
			return Response.noContent().build();
		}
		catch (Exception e)
		{
			TransactionUtil.rollback(tx);
			boolean isConstraintViolated = PersistenceUtil.isConstraintViolation(e);
			logger.warn(LOG_TAG, "update: " + e.getClass() + ", isConstraintViolated=" + isConstraintViolated);
			if (isConstraintViolated)
			{
				return Response.status(Status.CONFLICT).build();
			}
			throw new EJBException(e);
		}
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	public Response deleteById(@PathParam("id") Long id)
	{
    	if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "deleteById; id=" + id);
    	
		try
		{
			tx.begin();
			Employee entity = em.find(Employee.class, id);
			if (entity == null)
			{
				return Response.status(Status.NOT_FOUND).build();
			}
			em.remove(entity);
			tx.commit();
			return Response.noContent().build();
		}
		catch (Exception e)
		{
			TransactionUtil.rollback(tx);
			throw new EJBException(e);
		}
	}
	
    @GET
    @Path("/summary")
    @Produces("application/json")
    public Response summary()
    {
    	if (logger.isDebugEnabled())
			logger.debug(LOG_TAG + "summary");
    	
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> c = cb.createQuery(Long.class);
		Root<Employee> table = c.from(Employee.class);
		c.select(cb.count(table));
		TypedQuery<Long> tq = em.createQuery(c);
		long count = tq.getSingleResult().longValue();
        Calendar lastUpdate = new GregorianCalendar();
        EmployeeSummaryDTO dto = new EmployeeSummaryDTO(count, lastUpdate);
        return Response.ok(dto).build();
    }
}
