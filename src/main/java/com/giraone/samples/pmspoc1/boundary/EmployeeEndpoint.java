package com.giraone.samples.pmspoc1.boundary;

import java.sql.Connection;
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
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import com.giraone.samples.common.StringUtil;
import com.giraone.samples.common.boundary.BaseEndpoint;
import com.giraone.samples.common.boundary.PagingBlock;
import com.giraone.samples.common.boundary.model.ErrorInformation;
import com.giraone.samples.common.boundary.odata.ODataToJpaQueryBuilder;
import com.giraone.samples.common.entity.PersistenceUtil;
import com.giraone.samples.common.entity.UserTransactionConstraintViolationException;
import com.giraone.samples.common.entity.UserTransactionException;
import com.giraone.samples.common.entity.UserTransactional;
import com.giraone.samples.pmspoc1.boundary.blobs.BlobManager;
import com.giraone.samples.pmspoc1.boundary.blobs.BlobModelConfig;
import com.giraone.samples.pmspoc1.boundary.blobs.MimeTypeUtil;
import com.giraone.samples.pmspoc1.boundary.dto.CostCenterDTO;
import com.giraone.samples.pmspoc1.boundary.dto.EmployeeDTO;
import com.giraone.samples.pmspoc1.boundary.dto.EmployeeDocumentDTO;
import com.giraone.samples.pmspoc1.boundary.dto.EmployeePostalAddressDTO;
import com.giraone.samples.pmspoc1.boundary.dto.EmployeeSummaryDTO;
import com.giraone.samples.pmspoc1.boundary.dto.EmployeeWithPropertiesDTO;
import com.giraone.samples.pmspoc1.entity.CostCenter;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.EmployeeDocument;
import com.giraone.samples.pmspoc1.entity.EmployeeDocument_;
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
	final static BlobModelConfig BlobModelConfig = new BlobModelConfig(
		EmployeeDocument.class.getSimpleName(),
		EmployeeDocument_.SQL_NAME_oid,
		EmployeeDocument_.SQL_NAME_bytes,
		EmployeeDocument_.SQL_NAME_byteSize);

	
	@PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
	private EntityManager em;

	//-- SUMMARY ---------------------------------------------------------------------------------------------
	
    @GET
    @Path("/summary")
    @Produces("application/json; charset=UTF-8")
    public Response employeeSummary()
    {    	
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<Employee> table = countQuery.from(Employee.class);
		countQuery.select(cb.count(table));
		long count = em.createQuery(countQuery).getSingleResult().longValue();
        Calendar lastUpdate = new GregorianCalendar(); // Currently a simple time stamp is returned 
        EmployeeSummaryDTO dto = new EmployeeSummaryDTO(count, lastUpdate);
        return Response.ok(dto).build();
    }

	//-- EMPLOYEE --------------------------------------------------------------------------------------------

	/**
	 * Find an employee by its object id.
	 * @param id	The entity object id.
	 * @return	A found {@link EmployeeWithPropertiesDTO} object (status 200) or status "not found (404).
	 */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}")
	@Produces("application/json; charset=UTF-8")
	public Response findEmployeeById(@PathParam("employeeId") long employeeId,
		@QueryParam("expand") @DefaultValue("") String expand)
	{		
		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		// This is very import! We want the cost center object too (may be null) and use a left join
		table.fetch(Employee_.costCenter, JoinType.LEFT);
		
		final CriteriaQuery<Employee> select = c.select(table);
		final Predicate predicate = cb.equal(table.get(Employee_.oid), employeeId);
		select.where(predicate);
		
        if (StringUtil.isNotNullOrWhitespace(expand))
        {
        	ODataToJpaQueryBuilder<Employee> oDataBuilder = new ODataToJpaQueryBuilder<Employee>();
        	List<Attribute> expands = oDataBuilder.parseExpandExpression(cb, table, Employee_.class, expand);
			for (Attribute attribute : expands)
			{
				if (attribute instanceof PluralAttribute)
					table.fetch((PluralAttribute) attribute, JoinType.LEFT);
				else if (attribute instanceof SingularAttribute)
					table.fetch((SingularAttribute) attribute, JoinType.LEFT);	
			}
        }
        
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
    @Produces("application/json; charset=UTF-8")
    public Response findEmployeeByPersonnelNumber(@PathParam("personnelNumber") String personnelNumber,
    	@QueryParam("expand") @DefaultValue("") String expand)
    {    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
        final Root<Employee> table = c.from(Employee.class);
        
		// This is very import! We want the cost center object too (may be null) and use a left join
		// table.fetch(Employee_.costCenter, JoinType.LEFT);
		// No more needed since "expand" is used.
		
        final CriteriaQuery<Employee> select = c.select(table);
        final Predicate predicate = cb.equal(table.get(Employee_.personnelNumber), personnelNumber);
        select.where(predicate);
        
        if (StringUtil.isNotNullOrWhitespace(expand))
        {
        	ODataToJpaQueryBuilder<Employee> oDataBuilder = new ODataToJpaQueryBuilder<Employee>();
        	List<Attribute> expands = oDataBuilder.parseExpandExpression(cb, table, Employee_.class, expand);
			for (Attribute attribute : expands)
			{
				if (attribute instanceof PluralAttribute)
					table.fetch((PluralAttribute) attribute, JoinType.LEFT);
				else if (attribute instanceof SingularAttribute)
					table.fetch((SingularAttribute) attribute, JoinType.LEFT);	
			}
        }
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
     * @param top		OData paging
     * @param expand	OData expand expression
     * @return a {@link PagingBlock} object with {@link EmployeeDTO} object.
     */
	@GET
	@Produces("application/json; charset=UTF-8")
	public Response listEmployeeBlockwise(
		@QueryParam("filter") @DefaultValue("") String filter,
		@QueryParam("orderby") @DefaultValue("") String orderby,
		@QueryParam("skip") @DefaultValue("0") int skip,
		@QueryParam("top") @DefaultValue(DEFAULT_PAGING_SIZE) int top,
		@QueryParam("expand") @DefaultValue("") String expand)
	{		
		// TypedQuery<Employee> findAllQuery = em.createQuery(
		// "SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.costCenter ORDER
		// BY e.oid", Employee.class);
		// Join<Employee, CostCenter> join = table.join(Employee_.costCenter, JoinType.LEFT);

		final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
		final Root<Employee> table = c.from(Employee.class);
		
		ODataToJpaQueryBuilder<Employee> oDataBuilder = new ODataToJpaQueryBuilder<Employee>();
		
		// This is very import! We want the cost center object too (may be null) and use a left join
		// table.fetch(Employee_.costCenter, JoinType.LEFT);
		// No more needed since "expand" is used.
		
		if (StringUtil.isNotNullOrWhitespace(expand))
		{
			List<Attribute> expands = oDataBuilder.parseExpandExpression(cb, table, Employee_.class, expand);
			for (Attribute attribute : expands)
			{
				if (attribute instanceof PluralAttribute)
					table.fetch((PluralAttribute) attribute, JoinType.LEFT);
				else if (attribute instanceof SingularAttribute)
					table.fetch((SingularAttribute) attribute, JoinType.LEFT);	
			}
		}
		final CriteriaQuery<Employee> select = c.select(table);
		        
		Predicate predicate;
		try
		{
			predicate = oDataBuilder.parseFilterExpression(cb, table, filter);
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
		oDataBuilder.parseOrderExpression(cb, table, select, orderby);
		
		// Calculate the total count value using the same "table" - START		
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		countQuery.select(cb.count(table));
		if (predicate != null) { countQuery.where(predicate); }
		int totalCount = em.createQuery(countQuery).getSingleResult().intValue();
		// Calculate the total count value using the same "table" - END
		
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
	public Response createEmployee(EmployeeWithPropertiesDTO dto)
	{  
		final Employee entity;
		
		// Do not accept changes of member objects - in this case postal addresses
		if (dto.getPostalAddresses() != null)
			dto.getPostalAddresses().clear();
		// Do not accept changes of member objects - in this case documents
		if (dto.getDocuments() != null)
			dto.getDocuments().clear();
		
		try
		{
			entity = dto.entityFromDTO();
		}
		catch (IllegalArgumentException iae)
		{
			logger.warn(LOG_TAG, "create: invalid data for employee with personnelNumber=" + dto.getPersonnelNumber(), iae);
			ErrorInformation errorInformation = new ErrorInformation();
			errorInformation.setCode(ErrorInformation.FIELD_VALIDATION_FAILED);
			errorInformation.setMessage("invalid arguments");
			return Response.status(Status.BAD_REQUEST).entity(errorInformation).build();
		}
		
		// If cost center is given with an oid, it is not updated! It is fetched always fresh from the database.
		final CostCenterDTO costCenterDto = dto.getCostCenter();
		if (costCenterDto != null && costCenterDto.getOid() > 0)
		{
			entity.setCostCenter(fetchCostCenterOfEmployeeByOid(costCenterDto.getOid(), "personnelNumber" + dto.getPersonnelNumber()));
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
	public Response updateEmployee(@PathParam("employeeId") long employeeId, EmployeeWithPropertiesDTO dto)
	{    	
		if (dto == null || employeeId == 0)
		{
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (employeeId != dto.getOid())
		{
			logger.warn(LOG_TAG, "update: CONFLICT for employee with put.oid=" + employeeId + " not matching dto.oid=" + dto.getOid());
			ErrorInformation errorInformation = new ErrorInformation();
			errorInformation.setMessage("CONFLICT for employee with put.oid=" + employeeId + " not matching dto.oid=" + dto.getOid());
			return Response.status(Status.CONFLICT).entity(errorInformation).build();
		}

		Employee entity = em.find(Employee.class, employeeId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
			
		// Do not accept changes of member objects - in this case postal addresses
		if (dto.getPostalAddresses() != null)
			dto.getPostalAddresses().clear();
		// Do not accept changes of member objects - in this case documents
		if (dto.getDocuments() != null)
			dto.getDocuments().clear();
		
		// Now we have to fetch the properties by accessing at least one fake key (this is a bit weird in JPA!)
		entity.getProperties().get("");
		// Now we have to fetch the postal addresses
		entity.getPostalAddresses().size();
		// Now we have to fetch the existing cost center
		CostCenter existingCostCenter = entity.getCostCenter();
		
		// And now we merge changed data from the DTO (no cost center, no postal addresses)
		try
		{
			entity = dto.mergeFromDTO(entity, em);
		}
		catch (IllegalArgumentException iae)
		{
			logger.warn(LOG_TAG, "update: invalid data for employee with oid=" + employeeId, iae);
			ErrorInformation errorInformation = new ErrorInformation();
			errorInformation.setCode(ErrorInformation.FIELD_VALIDATION_FAILED);
			errorInformation.setMessage("invalid arguments");
			return Response.status(Status.BAD_REQUEST).entity(dto).build();
		}
				
		// If cost center is given, it cannot be updated! It is always fetched fresh from the database.
		final CostCenterDTO costCenterDto = dto.getCostCenter();
		if (costCenterDto != null)
		{
			if (costCenterDto.getOid() > 0)
			{
				// Was the cost center changed?
				if (existingCostCenter == null || costCenterDto.getOid() != existingCostCenter.getOid())
				{
					entity.setCostCenter(fetchCostCenterOfEmployeeByOid(costCenterDto.getOid(), "employeeId=" + employeeId));
				}
			}
			else
			{
				throw new IllegalArgumentException("update: employee costCenter oid is null for employeeId=" + employeeId);
			}
		}
		else
		{
			// Currently, we do not remove cost centers from the employee.
			//entity.setCostCenter(null); // Remove the cost center
		}
		
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
	public Response deleteEmployeeById(@PathParam("employeeId") long employeeId)
	{    	
		Employee entity = em.find(Employee.class, employeeId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	//-- ADDRESS ---------------------------------------------------------------------------------------------

	/**
	 * Find an employee address by its object id.
	 * @param employeeId	The employee's object id.
	 * @param addressId		The postal addresses object id.
	 * @return	A found {@link EmployeePostalAddressDTO} object (status 200) or status "not found (404).
	 */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/addresses/{addressId:[0-9][0-9]*}")
	@Produces("application/json; charset=UTF-8")
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
     * @param employeeId	The employee's object id.
     * @return list of {@link EmployeePostalAddressDTO} object.
     */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/addresses")
	@Produces("application/json; charset=UTF-8")
    public List<EmployeePostalAddressDTO> listAllPostalAddresses(@PathParam("employeeId") long employeeId)
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
	 * @param employeeId	The employee's object id.
	 * @param dto 			The new employee postal address data.
	 * @return Status CREATED (success), BAD_REQUEST (argument fault), NOT_FOUND (created but not found in DB);
	 */
	@POST
	@Path("/{employeeId:[0-9][0-9]*}/addresses")
	@Consumes("application/json")
	@UserTransactional
	public Response createPostalAddress(@PathParam("employeeId") long employeeId, EmployeePostalAddressDTO dto)
	{    			
		Employee employee = em.find(Employee.class, employeeId);
		if (employee == null)
		{			
			return Response.status(Status.NOT_FOUND).build();
		}		
		final EmployeePostalAddress entity = dto.entityFromDTO();
		// TODO: Concept for setting defaults is needed
		if (entity.getCountryCode() == null)
		{
			entity.setCountryCode("DE");
		}
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

	/**
	 * Update an employee's postal address using its object id.
	 * @param employeeId	The employee's object id.
	 * @param addressId		The postal addresses object id.
	 * @return NOT_FOUND (address not found), CONFLICT (id mismatch) or NO_CONTENT (success).
	 */
	@PUT
	@Path("/{employeeId:[0-9][0-9]*}/addresses/{addressId:[0-9][0-9]*}")
	@Consumes("application/json")
	@UserTransactional
	public Response updatePostalAddress(@PathParam("employeeId") long employeeId, @PathParam("addressId") long addressId,
		EmployeePostalAddressDTO dto)
	{    	
		if (dto == null || employeeId == 0L || addressId == 0L)
		{
			return Response.status(Status.BAD_REQUEST).build();
		}

		if (addressId != dto.getOid())
		{
			logger.warn(LOG_TAG, "update: CONFLICT PostalAddress put.addressId=" + addressId + " does not match dto.addressId=" + dto.getOid());
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
	 * @param employeeId	The employee's object id.
	 * @param addressId		The postal addresses object id.
	 * @return NOT_FOUND (address not found) or NO_CONTENT (success).
	 */
	@DELETE
	@Path("/{employeeId:[0-9][0-9]*}/addresses/{addressId:[0-9][0-9]*}")
	@UserTransactional
	public Response deletePostalAddressById(@PathParam("employeeId") long employeeId, @PathParam("addressId") long addressId)
	{    	
		EmployeePostalAddress entity = em.find(EmployeePostalAddress.class, addressId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}

	//-- DOCUMENT --------------------------------------------------------------------------------------------
    
	/**
	 * Find an employee's document by its object id.
	 * @param employeeId	The employee's object id.
	 * @param documentId	The document's object id.
	 * @return	A found {@link EmployeeDocumentDTO} object (status 200) or status "not found (404).
	 */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/documents/{documentId:[0-9][0-9]*}")
	@Produces("application/json; charset=UTF-8")
	public Response findDocumentById(@PathParam("employeeId") long employeeId, @PathParam("documentId") long documentId)
	{
		final EmployeeDocument entity = this.fetchEmployeeDocument(employeeId, documentId);
		if (entity != null)
		{
			EmployeeDocumentDTO dto = new EmployeeDocumentDTO(entity);
			return Response.ok(dto).build();
		}
		else
		{
			return Response.status(Status.NOT_FOUND).build();
		}
	}

    /**
     * List all documents of an employee ordered by their publishing date.
     * @param employeeId	The employee's object id.
     * @return list of {@link EmployeePostalAddressDTO} object.
     */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/documents")
	@Produces("application/json; charset=UTF-8")
    public List<EmployeeDocumentDTO> listAllDocuments(@PathParam("employeeId") long employeeId)
    {    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<EmployeeDocument> c = cb.createQuery(EmployeeDocument.class);
        final Root<EmployeeDocument> table = c.from(EmployeeDocument.class);
        final CriteriaQuery<EmployeeDocument> select = c.select(table);
        select.where(cb.equal(table.get(EmployeeDocument_.employee).get(Employee_.oid), employeeId));
        select.orderBy(cb.asc(table.get(EmployeeDocument_.publishingDate)));
        final TypedQuery<EmployeeDocument> tq = em.createQuery(select);              
        final List<EmployeeDocument> searchResults = tq.getResultList();
        final List<EmployeeDocumentDTO> results = new ArrayList<EmployeeDocumentDTO>();
        for (EmployeeDocument searchResult : searchResults)
        {
        	EmployeeDocumentDTO dto = new EmployeeDocumentDTO(searchResult);
            results.add(dto);
        }
        return results;
    }

    /**
     * Create a new employee document (without content.
     * @param employeeId	The employee's object id.
     * @param dto			The document's meta data.
	 * @return Status CREATED (success), NOT_FOUND (created but not found in DB);
     */
	@POST
	@Path("/{employeeId:[0-9][0-9]*}/documents")
	@Consumes("application/json")
	@UserTransactional
	public Response createDocument(@PathParam("employeeId") long employeeId, EmployeeDocumentDTO dto)
	{    			
		Employee employee = em.find(Employee.class, employeeId);
		if (employee == null)
		{			
			return Response.status(Status.NOT_FOUND).build();
		}		
		final EmployeeDocument entity = dto.entityFromDTO();
		entity.setEmployee(employee);
		entity.setByteSize(-1L);
		em.persist(entity);
		
		return Response
			.created(UriBuilder.fromResource(EmployeeEndpoint.class)
				.path(String.valueOf(employeeId))
				.path("documents")
				.path(String.valueOf(entity.getOid()))
				.build())
			.build();
	}


	/**
	 * Delete an employee document using its object id.
     * @param employeeId	The employee's object id.
     * @param documentId	The document's object id.
	 * @return NOT_FOUND (document not found) or NO_CONTENT (success).
	 */
	@DELETE
	@Path("/{employeeId:[0-9][0-9]*}/documents/{documentId:[0-9][0-9]*}")
	@UserTransactional
	public Response deleteDocumentById(@PathParam("employeeId") long employeeId, @PathParam("documentId") long documentId)
	{    	
		EmployeeDocument entity = em.find(EmployeeDocument.class, documentId);
		if (entity == null)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		em.remove(entity);
		return Response.noContent().build();
	}
	
	/**
	 * Add or update the BLOB content of an employee's document
     * @param employeeId	The employee's object id.
     * @param documentId	The document's object id.
	 * @param request		The servlet request object, from with the binary content is read.
	 * @return NOT_FOUND (document not found) or CREATED (success).
	 */
	@PUT
	@Path("/{employeeId:[0-9][0-9]*}/documents/{documentId:[0-9][0-9]*}/content")
	@UserTransactional
	public Response updateDocumentContent(
		@PathParam("employeeId") long employeeId,
		@PathParam("documentId") long documentId,
		@Context HttpServletRequest request)
	{
		int contentLength = request.getContentLength();
		String contentType = request.getContentType();
		
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "EmployeeEndpoint.updateDocumentContent contentLength=" + contentLength
				+ ", contentType=" + contentType + ", employeeId=" + employeeId + ", documentId=" + documentId);
		
		if (contentLength >= 0 && contentLength < 10) // plausibility check
		{
			return Response.status(Status.BAD_REQUEST).entity("ContentLength " + contentLength + " given, but lower than 10 bytes!").build();
		}
		BlobManager blobManager = new BlobManager();
		boolean ret;
		
		try
		{
			ret = blobManager.streamBlobToDatabase(this.em.unwrap(Connection.class), request.getInputStream(), contentLength, BlobModelConfig, documentId);
			if (logger != null && logger.isDebugEnabled())
				logger.debug(LOG_TAG, "EmployeeEndpoint.updateDocumentContent ret=" + ret);
		}
		catch (Exception e)
		{
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Cannot write BLOB of EmployeeDocument " + documentId
				+ ": " + e.getClass().getName() + " " + e.getMessage()).build();
		}
		if (!ret)
		{
			return Response.status(Status.NOT_FOUND).build();
		}
		
		return Response
			.created(UriBuilder.fromResource(EmployeeEndpoint.class)
				.path(String.valueOf(employeeId))
				.path("documents")
				.path(String.valueOf(documentId))
				.build())
			.build();
	}

	/**
	 * Stream/read the BLOB content of an employee's document. 
     * @param employeeId	The employee's object id.
     * @param documentId	The document's object id.
	 * @param response		The servlet response object, to with the binary content is streamed/written.
	 * @return NOT_FOUND (document not found), NO_CONTENT (BLOB content is empty) or OK (success).
	 */
	@GET
	@Path("/{employeeId:[0-9][0-9]*}/documents/{documentId:[0-9][0-9]*}/content")
	@UserTransactional // We need this because otherwise unwrap within BlobManager will not work!
	public void fetchDocumentContent(
		@PathParam("employeeId") final long employeeId,
		@PathParam("documentId") final long documentId,
		@Context final HttpServletResponse response)
	{
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "EmployeeEndpoint.fetchDocumentContent employeeId=" + employeeId + ", documentId=" + documentId);
				
		final BlobManager blobManager = new BlobManager();		
		final EmployeeDocument employeeDocument = this.fetchEmployeeDocument(employeeId, documentId);
		
		if (employeeDocument == null)
		{
			response.setStatus(Status.NOT_FOUND.getStatusCode());
			return;
		}
		
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "EmployeeEndpoint.fetchDocumentContent document.byteSize=" + employeeDocument.getByteSize());
		
		if (employeeDocument.getByteSize() == -1)
		{
			response.setStatus(Status.NO_CONTENT.getStatusCode());
			return;
		}
		
		String extension = MimeTypeUtil.getExtension(employeeDocument.getMimeType());
		String fileName = "EmployeeDocument_" + documentId + "." + extension;
		response.setContentType(employeeDocument.getMimeType());
	    response.setContentLength((int) employeeDocument.getByteSize());
		response.setHeader("Content-Disposition", "filename=" + fileName);
		response.setStatus(Status.OK.getStatusCode());
		
		/*
		This does not work, because the stream is processed asynchronously and the em/connection is closed then!
		StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream os) throws IOException, WebApplicationException {
            	long size;
				try
				{
					size = blobManager.streamBlobFromDatabase(response.getOutputStream(), BlobModelConfig, documentId);
				}
				catch (SQLException e)
				{
					e.printStackTrace();
					if (logger != null)
						logger.warn("Cannot read BLOB data for EmployeeDocument " + documentId, e);
					return;
				}
    			if (logger != null && logger.isDebugEnabled())
    				logger.debug(LOG_TAG, "EmployeeEndpoint.fetchDocumentContent " + size + " of " + employeeDocument.getDocumentBytesSize() + " Bytes sent.");
            }
        };       
        return Response.ok(stream).build();
        */
		
		// This is not yet perfect, because setStatus is called from the JAX/RS framework again, which leads to warnings:
		// "WARNING: Cannot set status. Response already committed."
		
		long size = -1L;
		try
		{
			size = blobManager.streamBlobFromDatabase(em.unwrap(Connection.class), response.getOutputStream(), BlobModelConfig, documentId);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (logger != null)
				logger.warn("Cannot read BLOB data for EmployeeDocument " + documentId, e);
			return;
		}
		if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "EmployeeEndpoint.fetchDocumentContent: " + size + " of " + employeeDocument.getByteSize() + " Bytes sent.");
	}
	
    //--------------------------------------------------------------------------------------------------------

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
    
    private EmployeeDocument fetchEmployeeDocument(long employeeId, long documentId)
    {
    	final CriteriaBuilder cb = em.getCriteriaBuilder();
		final CriteriaQuery<EmployeeDocument> c = cb.createQuery(EmployeeDocument.class);
		final Root<EmployeeDocument> table = c.from(EmployeeDocument.class);		
		final CriteriaQuery<EmployeeDocument> select = c.select(table);
		final Predicate predicate = cb.and(
			cb.equal(table.get(EmployeeDocument_.employee).get(Employee_.oid), employeeId),
			cb.equal(table.get(EmployeeDocument_.oid), documentId));
		select.where(predicate);
		final TypedQuery<EmployeeDocument> tq = em.createQuery(select);

		return PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
			EmployeeDocument_.SQL_NAME_oid);
    }
}