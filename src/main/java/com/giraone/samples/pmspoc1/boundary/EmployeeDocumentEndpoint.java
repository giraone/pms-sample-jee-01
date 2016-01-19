package com.giraone.samples.pmspoc1.boundary;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.giraone.samples.common.boundary.BaseEndpoint;
import com.giraone.samples.common.boundary.MultipartRequestMap;
import com.giraone.samples.common.entity.UserTransactional;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.EmployeeDocument;

/**
 * REST end point for CRUD operations on "cost center" entities.
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@Path("/employees/documents")
public class EmployeeDocumentEndpoint extends BaseEndpoint
{
	private static final Marker LOG_TAG = MarkerManager.getMarker("DOCS");
	
	@Inject
	private Logger logger;
	
    @PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
    private EntityManager em;
    
    @POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json; charset=UTF-8")
    @UserTransactional
	public Response uploadFileUsingMultipartPost(@Context HttpServletRequest request)
	{
    	MultipartRequestMap map;
		try
		{
			map = new MultipartRequestMap(request);
		}
		catch (Exception e)
		{
			logger.error(LOG_TAG, "EmployeeDocumentEndpoint.uploadFileUsingMultipartPost: Cannot parse multi-part", e);
			return Response.status(Response.Status.BAD_REQUEST).entity("Cannot parse multi-part: " + e.getMessage()).build();
		}

    	logger.debug(LOG_TAG, "CCCCCCCCCCCCCC " + map);
    	long employeeId = Long.parseLong(map.getStringParameter("employeeId"));
    	String businessType = map.getStringParameter("businessType");
    	String mimeType = map.getStringParameter("mimeType");
    	File file = map.getFileParameter("file");
    	if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "employeeId=" + employeeId);
			logger.debug(LOG_TAG, "businessType=" + businessType);
			logger.debug(LOG_TAG, "mimeType=" + mimeType);
			logger.debug(LOG_TAG, "file=" + file);
		}
		if (file == null)
		{
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "#EmployeeDocumentEndpoint.uploadFileUsingMultipartPost#" + file.getAbsolutePath().substring(0, Math.min(file.getAbsolutePath().length(), 80)) + "#");
		}
		
		EmployeeDocument document = new EmployeeDocument();
		Employee employee = em.find(Employee.class, employeeId);
		if (employee == null)
		{
			return Response.status(Response.Status.NOT_FOUND).entity("Employee " + employeeId + " not found!").build();
		}
		
		document.setEmployee(employee);
		document.setBusinessType(businessType);
		//TODO: MIME Type check
		document.setMimeType(mimeType);
		document.setDocumentBytesSize(file.length());
		
		try
		{
			return this.readAndSave(document, file);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
    /*
    // Leads to ==> Illegal URL-encoded characters, make sure that no @FormParam and @Multipart annotations are mixed up
    
    @POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json; charset=UTF-8")
	public Response uploadFileUsingMultipartPost(
		@FormParam("employeeId") long employeeId,
		@FormParam("businessType") String businessType,
		@FormParam("mimeType") String mimeType,
		@FormParam("file") File file)
	{
		if (file == null)
		{
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
		System.err.println("#EmployeeDocumentEndpoint.uploadFileUsingMultipartPost#" + file.getAbsolutePath().substring(0, Math.min(file.getAbsolutePath().length(), 80)) + "#");
		
		EmployeeDocument document = new EmployeeDocument();
		Employee employee = em.find(Employee.class, employeeId);
		if (employee == null)
		{
			return Response.status(Response.Status.NOT_FOUND).entity("Employee " + employeeId + " not found!").build();
		}
		
		document.setEmployee(employee);
		document.setBusinessType(businessType);
		//TODO: MIME Type check
		document.setMimeType(mimeType);
		document.setDocumentBytesSize(file.length());
		
		try
		{
			return this.readAndSave(document, file);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
    */
    
    protected Response readAndSave(EmployeeDocument document, File file) throws URISyntaxException
	{
		FileInputStream in;
		try
		{
			in = new FileInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return Response.status(Status.NOT_FOUND).entity(e.getMessage()).build();
		}
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			long size = pipeBlob(in, out);
			if (size != document.getDocumentBytesSize())
			{
				throw new IllegalStateException("File " + file.getAbsolutePath() + " was " + document.getDocumentBytesSize()
					+ "Bytes. Buffer read was " + size + " Bytes!");
			}
			document.setDocumentBytes(out.toByteArray());
			em.persist(document);
			return Response.created(new URI("./" + document.getOid())).build();
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
    
    private static long pipeBlob(InputStream in, OutputStream out)
	{
		byte[] buffer = new byte[4096];
		long total = 0;
		int r;
		
		try
		{
			while ((r = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, r);
				total += r;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -1;
		}
		finally
		{
			try
			{
				in.close();
			}
			catch (IOException ignore)
			{
			}
		}
		
		return total;
	}
}