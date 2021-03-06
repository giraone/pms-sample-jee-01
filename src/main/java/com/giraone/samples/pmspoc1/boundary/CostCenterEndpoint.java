package com.giraone.samples.pmspoc1.boundary;

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

import com.giraone.samples.common.boundary.BaseEndpoint;
import com.giraone.samples.common.boundary.odata.ODataToJpaQueryBuilder;
import com.giraone.samples.common.entity.PersistenceUtil;
import com.giraone.samples.common.entity.UserTransactionConstraintViolationException;
import com.giraone.samples.common.entity.UserTransactionException;
import com.giraone.samples.common.entity.UserTransactional;
import com.giraone.samples.pmspoc1.boundary.PmsCoreApi;
import com.giraone.samples.pmspoc1.boundary.dto.CostCenterDTO;
import com.giraone.samples.pmspoc1.boundary.dto.CostCenterSummaryDTO;
import com.giraone.samples.pmspoc1.entity.CostCenter;
import com.giraone.samples.pmspoc1.entity.CostCenter_;

/**
 * REST end point for CRUD operations on "cost center" entities.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("/costcenters")
public class CostCenterEndpoint extends BaseEndpoint
{	
    @PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
    private EntityManager em;

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("application/json; charset=UTF-8")
    public Response findById(@PathParam("id") long id)
    {    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<CostCenter> c = cb.createQuery(CostCenter.class);
        final Root<CostCenter> table = c.from(CostCenter.class);
        final CriteriaQuery<CostCenter> select = c.select(table);
        final Predicate predicate = cb.equal(table.get(CostCenter_.oid), id);
        select.where(predicate);
        final TypedQuery<CostCenter> tq = em.createQuery(select);

        final CostCenter entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
            CostCenter_.SQL_NAME_oid);
        if (entity != null)
        {
            CostCenterDTO dto = new CostCenterDTO(entity);
            return Response.ok(dto).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * Additional GET method using the also unique "identification" attribute prefixed with "id-"
     * 
     * @param identification
     * @return CostCenterDTO object
     */
    @GET
    @Path("/id-{identification:[0-9a-zA-Z][0-9a-zA-Z]*}")
    @Produces("application/json; charset=UTF-8")
    public Response findByIdentification(@PathParam("identification") String identification)
    {    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<CostCenter> c = cb.createQuery(CostCenter.class);
        final Root<CostCenter> table = c.from(CostCenter.class);
        final CriteriaQuery<CostCenter> select = c.select(table);
        final Predicate predicate = cb.equal(table.get(CostCenter_.identification), identification);
        select.where(predicate);
        final TypedQuery<CostCenter> tq = em.createQuery(select);

        final CostCenter entity = PersistenceUtil.sanityCheckForSingleResultList(tq.getResultList(),
            CostCenter_.SQL_NAME_identification);
        if (entity != null)
        {
            CostCenterDTO dto = new CostCenterDTO(entity);
            return Response.ok(dto).build();
        }
        else
        {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    /**
     * List all cost centers with support for OData filters.
     * @param filter	OData filter expression
     * @param orderby	OData sort expression
     * @param skip		OData paging
     * @param top		OData filter expression
     * @return List of {link @CostCenterDTO} objects.
     */
    @GET
    @Produces("application/json; charset=UTF-8")
    public List<CostCenterDTO> listAll(
    	@QueryParam("filter") @DefaultValue("") String filter,
		@QueryParam("orderby") @DefaultValue("") String orderby,
		@QueryParam("skip") @DefaultValue("0") int skip,
		@QueryParam("top") @DefaultValue(DEFAULT_PAGING_SIZE) int top)
    {    	
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<CostCenter> c = cb.createQuery(CostCenter.class);
        final Root<CostCenter> table = c.from(CostCenter.class);
        final CriteriaQuery<CostCenter> select = c.select(table);
        
        ODataToJpaQueryBuilder<CostCenter> filterBuilder = new ODataToJpaQueryBuilder<CostCenter>();

		Predicate predicate = filterBuilder.parseFilterExpression(cb, table, filter);
		if (predicate != null) { select.where(predicate); }
		filterBuilder.parseOrderExpression(cb, table, select, orderby);
		
        final TypedQuery<CostCenter> tq = em.createQuery(select);
        tq.setFirstResult(skip);
        tq.setMaxResults(top);
                
        final List<CostCenter> searchResults = tq.getResultList();
        final List<CostCenterDTO> results = new ArrayList<CostCenterDTO>();
        for (CostCenter searchResult : searchResults)
        {
            CostCenterDTO dto = new CostCenterDTO(searchResult);
            results.add(dto);
        }
        return results;
    }

    @POST
    @Consumes("application/json")
    @UserTransactional
    public Response create(CostCenterDTO dto)
    {    	
        CostCenter entity = dto.entityFromDTO();
        em.persist(entity);
        return Response
            .created(
                UriBuilder.fromResource(CostCenterEndpoint.class).path(String.valueOf(entity.getOid())).build())
            .build();
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/json")
    @UserTransactional
    public Response update(@PathParam("id") long id, CostCenterDTO dto)
    {    	
        if (dto == null || id == 0)
        {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (id != dto.getOid())
        {
        	logger.warn(LOG_TAG, "update: costCenter CONFLICT put.oid=" + id + " does not match dto.oid=" + dto.getOid());
            return Response.status(Status.CONFLICT).entity(dto).build();
        }

        CostCenter entity = em.find(CostCenter.class, id);
        if (entity == null)
        {
            return Response.status(Status.NOT_FOUND).build();
        }                       
        entity = dto.mergeFromDTO(entity, em);
        entity = em.merge(entity);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id:[0-9][0-9]*}")
    @UserTransactional
    public Response deleteById(@PathParam("id") long id)
    {    	 
        CostCenter entity = em.find(CostCenter.class, id);
        if (entity == null)
        {
            return Response.status(Status.NOT_FOUND).build();
        }           
        em.remove(entity);
        return Response.noContent().build();
    }
    
    @GET
    @Path("/summary")
    @Produces("application/json; charset=UTF-8")
    public Response summary()
    {
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<CostCenter> table = countQuery.from(CostCenter.class);
		countQuery.select(cb.count(table));
		TypedQuery<Long> tq = em.createQuery(countQuery);
		long count = tq.getSingleResult().longValue();
        Calendar lastUpdate = new GregorianCalendar(); // Currently a simple time stamp is returned 
        CostCenterSummaryDTO dto = new CostCenterSummaryDTO(count, lastUpdate);
        return Response.ok(dto).build();
    }
    
    public Object handleUserTransactionException(UserTransactionException userTransactionException)
    {
    	if (logger != null && logger.isDebugEnabled())
			logger.debug(LOG_TAG, "CostCenterEndpoint.handleTransactionException " + userTransactionException);
    	
    	if (userTransactionException instanceof UserTransactionConstraintViolationException)
    		return Response.status(HTTP_UNPROCESSABLE).build();
    	else
    		return Response.status(Status.CONFLICT).build();
    }
}