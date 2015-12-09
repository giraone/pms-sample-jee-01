package com.giraone.samples.pmspoc1.boundary.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
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
import com.giraone.samples.pmspoc1.boundary.core.dto.CostCenterDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeePostalAddressDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeSummaryDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.EmployeeWithPropertiesDTO;
import com.giraone.samples.pmspoc1.entity.CostCenter;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress;
import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress_;
import com.giraone.samples.pmspoc1.entity.Employee_;

/**
 * REST end point for CRUD operations on "employee" entities.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("/employees")
public class EmployeeEndpoint extends BaseEndpoint
{		
	@PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
	private EntityManager em;

	/**
	 * Find an employee by its object id.
	 * @param id	The entity object id.
	 * @return	A found {@link EmployeeWithPropertiesDTO} object (status 200) or status "not found (404).
	 */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findById(@PathParam("employeeId") long employeeId)
	{		
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		// This is very import! We want the cost center object too (may be null) and use a left join
		table.fetch(Employee_.costCenter, JoinType.LEFT);
		
		final CriteriaQuery<Employee> select = c.select(table);
		final Predicate predicate = cb.equal(table.get(Employee_.oid), employeeId);
		select.where(predicate);
		final TypedQuery<Employee> tq = em.createQuery(select);

		final Employee entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
			Employee_.SQL_NAME_oid);
		if (entity != null)
		{
			// Now we have to fetch the properties by accessing at least one fake key (this is a bit weird in JPA!)
			entity.getProperties().get("");
			// Now we have to fetch the postal addresses
			entity.getPostalAddresses().size();
			if (logger.isDebugEnabled())
			{
				logger.debug("Employee.findById=isLoaded(addresses)" + Persistence.getPersistenceUtil().isLoaded(entity, "addresses"));
			}
			EmployeeWithPropertiesDTO dto = new EmployeeWithPropertiesDTO(entity);
			
			return Response.ok(dto).build();
		}
		else
		{
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	/**
	 * Alternate find method to find an employee by its unique "personnelNumber".
     * @param personnelNumber (this path parameter prefixed with "pnr-")
	 * @return	A found {@link EmployeeWithPropertiesDTO} object (status 200) or status "not found (404).
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
        select.where(predicate);
        final TypedQuery<Employee> tq = em.createQuery(select);

        final Employee entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
        	Employee_.SQL_NAME_personnelNumber);
        if (entity != null)
        {
			// Now we have to fetch the properties by accessing at least one fake key (this is a bit weird in JPA!)
			entity.getProperties().get("");
			// Now we have to fetch the postal addresses
			entity.getPostalAddresses().size();
			
			EmployeeWithPropertiesDTO dto = new EmployeeWithPropertiesDTO(entity);
			
            return Response.ok(dto).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).build();
        }
    }
    
    /**
     * List all employees with support for OData filters.
     * @param filter	OData filter expression
     * @param orderby	OData sort expression
     * @param skip		OData paging
     * @param top		OData filter expression
     * @return a {@link PagingBlock} object with {@link EmployeeDTO} object.
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

	/**
	 * Create a new entity
	 * @param dto the new employee's data.
	 * @return Status CREATED (success), BAD_REQUEST (argument fault),
	 * CONFLICT (duplicate personnelNumber) or NOT_FOUND (created but not found in DB);
	 */
	@POST
	@Consumes("application/json")
	@UserTransactional
	public Response create(EmployeeWithPropertiesDTO dto)
	{  
		final Employee entity = dto.entityFromDTO();
		
		// If cost center is given without versionNumber, it cannot be updated! It is fetched fresh from the database.
		final CostCenter costCenter = entity.getCostCenter();
		if (costCenter != null && costCenter.getOid() > 0 && costCenter.getVersionNumber() < 0)
		{
			entity.setCostCenter(fetchCostCenterOfEmployeeByOid(costCenter.getOid(), "personnelNumber" + dto.getPersonnelNumber()));
		}
		em.persist(entity);
		
		return Response
			.created(UriBuilder.fromResource(EmployeeEndpoint.class).path(String.valueOf(entity.getOid())).build())
			.build();
	}

	@PUT
	@Path("/{employeeId:[0-9][0-9]*}")
	@Consumes("application/json")
	@UserTransactional
	public Response update(@PathParam("employeeId") long employeeId, EmployeeWithPropertiesDTO dto)
	{    	
		if (dto == null || employeeId == 0)
		{
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (employeeId != dto.getOid())
		{
			logger.warn(LOG_TAG, "update CONFLICT employeeId=" + employeeId + ", employeeId2=" + dto.getOid());
			return Response.status(Status.CONFLICT).entity(dto).build();
		}

		Employee entity = em.find(Employee.class, employeeId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		
		// If cost center is given regardless of the version number, it cannot be updated! It is always fetched fresh from the database.
		final CostCenterDTO costCenterDto = dto.getCostCenter();
		if (costCenterDto != null && costCenterDto.getOid() > 0)
		{
			entity.setCostCenter(fetchCostCenterOfEmployeeByOid(costCenterDto.getOid(), "employeeId=" + employeeId));
		}
				
		// Now we have to fetch the properties by accessing at least one fake key (this is a bit weird in JPA!)
		entity.getProperties().get("");
		// Now we have to fetch the postal addresses
		entity.getPostalAddresses().size();
		// And now we merge changed data from the DTO
		entity = dto.mergeFromDTO(entity, em);
		// and persist everything
		entity = em.merge(entity);
		
		return Response.noContent().build();
	}

	/**
	 * Delete an employee using its object id.
	 * @param id	the employee object id.
	 * @return NOT_FOUND (employee not found) or NO_CONTENT (success).
	 */
	@DELETE
	@Path("/{employeeId:[0-9][0-9]*}")
	@UserTransactional
	public Response deleteById(@PathParam("employeeId") long employeeId)
	{    	
		Employee entity = em.find(Employee.class, employeeId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	/**
	 * Find an employee address by its object id.
	 * @param id	The entity object id.
	 * @return	A found {@link EmployeePostalAddressDTO} object (status 200) or status "not found (404).
	 */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/addresses/{addressId:[0-9][0-9]*}")
	@Produces("application/json")
	public Response findPostalAddressById(@PathParam("employeeId") long employeeId, @PathParam("addressId") long addressId)
	{
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<EmployeePostalAddress> c = cb.createQuery(EmployeePostalAddress.class);
		final Root<EmployeePostalAddress> table = c.from(EmployeePostalAddress.class);		
		final CriteriaQuery<EmployeePostalAddress> select = c.select(table);
		final Predicate predicate = cb.and(
			cb.equal(table.get(EmployeePostalAddress_.employee).get(Employee_.oid), employeeId),
			cb.equal(table.get(EmployeePostalAddress_.oid), addressId));
		select.where(predicate);
		final TypedQuery<EmployeePostalAddress> tq = em.createQuery(select);

		final EmployeePostalAddress entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
			EmployeePostalAddress_.SQL_NAME_oid);
		if (entity != null)
		{
			EmployeePostalAddressDTO dto = new EmployeePostalAddressDTO(entity);
			return Response.ok(dto).build();
		}
		else
		{
			return Response.status(Status.NOT_FOUND).build();
		}
	}

    /**
     * List all postal addresses of an employee ordered by their ranking.
     * @return list of {@link EmployeePostalAddressDTO} object.
     */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/addresses")
	@Produces("application/json")
    public List<EmployeePostalAddressDTO> listAll(@PathParam("employeeId") long employeeId)
    {    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<EmployeePostalAddress> c = cb.createQuery(EmployeePostalAddress.class);
        final Root<EmployeePostalAddress> table = c.from(EmployeePostalAddress.class);
        final CriteriaQuery<EmployeePostalAddress> select = c.select(table);
        select.where(cb.equal(table.get(EmployeePostalAddress_.employee).get(Employee_.oid), employeeId));
        select.orderBy(cb.asc(table.get(EmployeePostalAddress_.ranking)));
        final TypedQuery<EmployeePostalAddress> tq = em.createQuery(select);              
        final List<EmployeePostalAddress> searchResults = tq.getResultList();
        final List<EmployeePostalAddressDTO> results = new ArrayList<EmployeePostalAddressDTO>();
        for (EmployeePostalAddress searchResult : searchResults)
        {
        	EmployeePostalAddressDTO dto = new EmployeePostalAddressDTO(searchResult);
            results.add(dto);
        }
        return results;
    }
 
	/**
	 * Create a new employee address
	 * @param dto the new employee postal address data.
	 * @return Status CREATED (success), BAD_REQUEST (argument fault), NOT_FOUND (created but not found in DB);
	 */
	@POST
	@Path("/{employeeId:[0-9][0-9]*}/addresses")
	@Consumes("application/json")
	@UserTransactional
	public Response create(@PathParam("employeeId") long employeeId, EmployeePostalAddressDTO dto)
	{    			
		Employee employee = em.find(Employee.class, employeeId);
		if (employee == null)
		{			
			return Response.status(Status.NOT_FOUND).build();
		}		
		final EmployeePostalAddress entity = dto.entityFromDTO();
		entity.setEmployee(employee);
		em.persist(entity);
		
		return Response
			.created(UriBuilder.fromResource(EmployeeEndpoint.class)
				.path(String.valueOf(employeeId))
				.path("addresses")
				.path(String.valueOf(entity.getOid()))
				.build())
			.build();
	}

	@PUT
	@Path("/{employeeId:[0-9][0-9]*}/addresses/{addressId:[0-9][0-9]*}")
	@Consumes("application/json")
	@UserTransactional
	public Response update(@PathParam("employeeId") long employeeId, @PathParam("addressId") long addressId,
		EmployeePostalAddressDTO dto)
	{    	
		if (dto == null || employeeId == 0L || addressId == 0L)
		{
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (addressId != dto.getOid())
		{
			logger.warn(LOG_TAG, "update CONFLICT addressId=" + addressId + ", addressId2=" + dto.getOid());
			return Response.status(Status.CONFLICT).entity(dto).build();
		}

		/*
		if (!employeeId.equals(dto.getEmployeeId()))
		{
			logger.warn(LOG_TAG, "update CONFLICT employeeId=" + employeeId + ", employeeId2=" + dto.getEmployeeId());
			return Response.status(Status.CONFLICT).entity(dto).build();
		}
		*/
		
		EmployeePostalAddress entity = em.find(EmployeePostalAddress.class, addressId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		entity = dto.mergeFromDTO(entity, em);
		entity = em.merge(entity);
		
		return Response.noContent().build();
	}

	/**
	 * Delete an employee's postal address using its object id.
	 * @param id	the postal address object id.
	 * @return NOT_FOUND (address not found) or NO_CONTENT (success).
	 */
	@DELETE
	@Path("/{employeeId:[0-9][0-9]*}/addresses/{addressId:[0-9][0-9]*}")
	@UserTransactional
	public Response deleteById(@PathParam("employeeId") long employeeId, @PathParam("addressId") long addressId)
	{    	
		EmployeePostalAddress entity = em.find(EmployeePostalAddress.class, addressId);
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
    		return Response.status(HTTP_UNPROCESSABLE).build();
    	else
    		return Response.status(Status.CONFLICT).build();
    }
    
    private CostCenter fetchCostCenterOfEmployeeByOid(long costCenterOid, String employeeContext)
    {
    	final CostCenter costCenter = em.find(CostCenter.class, costCenterOid);
		if (costCenter == null)
		{
			logger.warn(LOG_TAG, "INVALID costCenterOid=" + costCenterOid + " for " + employeeContext);
			throw new IllegalArgumentException("No costcenter with oid=" + costCenterOid + " found!");
		}
		return costCenter;
    }
}