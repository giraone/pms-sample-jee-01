package com.giraone.samples.pmspoc1.boundary.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
import javax.ws.rs.core.UriBuilder;

import com.giraone.samples.common.StringUtil;
import com.giraone.samples.common.boundary.BaseEndpoint;
import com.giraone.samples.common.boundary.PagingBlock;
import com.giraone.samples.common.boundary.odata.ODataToJpaQueryBuilder;
import com.giraone.samples.common.entity.PersistenceUtil;
import com.giraone.samples.common.entity.UserTransactionConstraintViolationException;
import com.giraone.samples.common.entity.UserTransactionException;
import com.giraone.samples.common.entity.UserTransactional;
import com.giraone.samples.pmspoc1.boundary.PmsCoreApi;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeSummaryDTO;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.Employee_;

/**
 * REST end point for CRUD operations on "employee" entities.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("/employees")
public class EmployeeEndpoint extends BaseEndpoint// implements UserTransactionExceptionHandler
{		
	@PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
	private EntityManager em;

	@GET
	@Path("/{id:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("id") Long id)
	{		
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		// This is very import! We want the cost center object too (may be null) and use a left join
		table.fetch(Employee_.costCenter, JoinType.LEFT);
		
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
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        final Root<Employee> table = c.from(Employee.class);
		// This is very import! We want the cost center object too (may be null) and use a left join
		table.fetch(Employee_.costCenter, JoinType.LEFT);
        
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
    
    /**
     * List all entities with support for OData filters.
     * @param filter	OData filter expression
     * @param orderby	OData sort expression
     * @param skip		OData paging
     * @param top		OData filter expression
     * @return a {@link PagingBlock} object.
     */
	@GET
	@Produces("application/json")
	public Response listBlock(
		@QueryParam("filter") @DefaultValue("") String filter,
		@QueryParam("orderby") @DefaultValue("") String orderby,
		@QueryParam("skip") @DefaultValue("0") int skip,
		@QueryParam("top") @DefaultValue(DEFAULT_PAGING_SIZE) int top)
	{		
		// TypedQuery<Employee> findAllQuery = em.createQuery(
		// "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.costCenter ORDER
		// BY e.oid", Employee.class);
		// Join<Employee, CostCenter> join = table.join(Employee_.costCenter, JoinType.LEFT);

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		// This is very import! We want the cost center object too (may be null) and use a left join
		table.fetch(Employee_.costCenter, JoinType.LEFT);
		
		final CriteriaQuery<Employee> select = c.select(table);
		
        ODataToJpaQueryBuilder<Employee> filterBuilder = new ODataToJpaQueryBuilder<Employee>();
		filterBuilder.setCriteriaTable(cb, table);
		Predicate predicate;
		try
		{
			predicate = filterBuilder.parseFilterExpression(filter);
		}
		catch (IllegalArgumentException iae)
		{
			// OData arguments are illegal
			if (logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG, "Illegal OData filter=" + StringUtil.serializeAsJavaString(filter), iae);
			}
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (predicate != null) { select.where(predicate); }
		filterBuilder.parseOrderExpression(cb, select, orderby);
		
		// Calculating the total count value - START
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		countQuery.select(cb.count(table));
		if (predicate != null) { countQuery.where(predicate); }
		int totalCount = em.createQuery(countQuery).getSingleResult().intValue();
		// Calculating the total count value - END
		
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
		PagingBlock<EmployeeDTO> pagingBlock = new PagingBlock<EmployeeDTO>();
		pagingBlock.setBlockCounter(skip);
		pagingBlock.setBlockSize(top);
		pagingBlock.setTotalCount(totalCount);
		pagingBlock.setBlockItems(results);
		return Response.ok(pagingBlock).build();
	}

	@POST
	@Consumes("application/json")
	@UserTransactional
	public Response create(EmployeeDTO dto)
	{    	
		final Employee entity =  dto.fromDTO(null, em);
		em.persist(entity);
		
		return Response
			.created(UriBuilder.fromResource(EmployeeEndpoint.class).path(String.valueOf(entity.getOid())).build())
			.build();
	}

	@PUT
	@Path("/{id:[0-9][0-9]*}")
	@Consumes("application/json")
	@UserTransactional
	public Response update(@PathParam("id") Long id, EmployeeDTO dto)
	{    	
		if (dto == null || id == null)
		{
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (!id.equals(dto.getOid()))
		{
			logger.warn(LOG_TAG, "update CONFLICT id1=" + id + ", id2=" + dto.getOid());
			return Response.status(Status.CONFLICT).entity(dto).build();
		}

		Employee entity = em.find(Employee.class, id);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		entity = dto.fromDTO(entity, em);
		entity = em.merge(entity);
		
		return Response.noContent().build();
	}

	@DELETE
	@Path("/{id:[0-9][0-9]*}")
	@UserTransactional
	public Response deleteById(@PathParam("id") Long id)
	{    	
		Employee entity = em.find(Employee.class, id);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}
	
    @GET
    @Path("/summary")
    @Produces("application/json")
    public Response summary()
    {    	
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
    
    public Object handleUserTransactionException(UserTransactionException userTransactionException)
    {
    	if (logger.isDebugEnabled())
			logger.debug(LOG_TAG, "EmployeeEndpoint.handleTransactionException " + userTransactionException);
    	
    	if (userTransactionException instanceof UserTransactionConstraintViolationException)
    		return Response.status(422).build();
    	else
    		return Response.status(Status.CONFLICT).build();
    }
}
