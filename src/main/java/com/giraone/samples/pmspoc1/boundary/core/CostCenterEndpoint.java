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
import javax.persistence.EntityManager;
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

import com.giraone.samples.pmspoc1.boundary.PmsCoreApi;
import com.giraone.samples.pmspoc1.boundary.core.dto.CostCenterDTO;
import com.giraone.samples.pmspoc1.boundary.core.dto.CostCenterSummaryDTO;
import com.giraone.samples.pmspoc1.control.PersistenceUtil;
import com.giraone.samples.pmspoc1.control.TransactionUtil;
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
    @Resource
    UserTransaction tx;

    @PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
    private EntityManager em;

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces("application/json")
    public Response findById(@PathParam("id") Long id)
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<CostCenter> c = cb.createQuery(CostCenter.class);
        final Root<CostCenter> table = c.from(CostCenter.class);
        final CriteriaQuery<CostCenter> select = c.select(table);
        final Predicate predicate = cb.equal(table.get(CostCenter_.oid), id);
        // Open issue: Does it make any sense to use also .distinct() here?
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
    @Produces("application/json")
    public Response findByIdentification(@PathParam("identification") String identification)
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<CostCenter> c = cb.createQuery(CostCenter.class);
        final Root<CostCenter> table = c.from(CostCenter.class);
        final CriteriaQuery<CostCenter> select = c.select(table);
        final Predicate predicate = cb.equal(table.get(CostCenter_.identification), identification);
        // Open issue: Does it make any sense to use also .distinct() here?
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

    @GET
    @Produces("application/json")
    public List<CostCenterDTO> listAll(@QueryParam("start") Integer startPosition, @QueryParam("max") Integer maxResult)
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<CostCenter> c = cb.createQuery(CostCenter.class);
        final Root<CostCenter> table = c.from(CostCenter.class);
        final CriteriaQuery<CostCenter> select = c.select(table);
        // TODO: No filtering yet
        /*
         * final Predicate predicate = null; if (predicate != null) { select.where(predicate); }
         */
        select.orderBy(cb.asc(table.get(CostCenter_.oid)));
        final TypedQuery<CostCenter> tq = em.createQuery(select);

        if (startPosition != null)
        {
            tq.setFirstResult(startPosition);
        }
        if (maxResult != null)
        {
            tq.setMaxResults(maxResult);
        }
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
    public Response create(CostCenterDTO dto)
    {
        try
        {
            tx.begin();
            CostCenter entity = dto.fromDTO(null, em);
            em.persist(entity);
            tx.commit();
            return Response
                .created(
                    UriBuilder.fromResource(CostCenterEndpoint.class).path(String.valueOf(entity.getOid())).build())
                .build();
        }
        catch (Exception e)
        {
        	TransactionUtil.rollback(tx);
            boolean isConstraintViolated = PersistenceUtil.isConstraintViolation(e);
            if (isConstraintViolated)
            {
                return Response.status(Status.CONFLICT).build();
            }
            throw new EJBException(e);
        }
    }

    @PUT
    @Path("/{id:[0-9][0-9]*}")
    @Consumes("application/json")
    public Response update(@PathParam("id") Long id, CostCenterDTO dto)
    {
        //System.err.println("CostCenterEndpoint.update " + id + " " + dto);
        if (dto == null || id == null)
        {
            return Response.status(Status.BAD_REQUEST).build();
        }

        if (!id.equals(dto.getOid()))
        {
        	System.err.println("CostCenterEndpoint.update CONFLICT");
            return Response.status(Status.CONFLICT).entity(dto).build();
        }

        try
        {  
        	tx.begin();
            CostCenter entity = em.find(CostCenter.class, id);
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
            	System.err.println("CostCenterEndpoint.update OptimisticLockException");
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
            System.err.println("CostCenterEndpoint.update: " + e.getClass() + ", isConstraintViolated=" + isConstraintViolated);
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
        try
        {  
        	tx.begin();
            CostCenter entity = em.find(CostCenter.class, id);
            if (entity == null)
            {
            	tx.commit();
                return Response.status(Status.NOT_FOUND).build();
            }           
            em.remove(entity);
            //System.err.println("CostCenterEndpoint.deleteById " + id);
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
    	CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> c = cb.createQuery(Long.class);
		Root<CostCenter> table = c.from(CostCenter.class);
		c.select(cb.count(table));
		TypedQuery<Long> tq = em.createQuery(c);
		long count = tq.getSingleResult().longValue();
        Calendar lastUpdate = new GregorianCalendar();
        CostCenterSummaryDTO dto = new CostCenterSummaryDTO(count, lastUpdate);
        return Response.ok(dto).build();
    }
}
