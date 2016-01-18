package com.giraone.samples.pmspoc1.boundary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.giraone.samples.common.entity.UserTransactional;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.EmployeeDocument;

/**
 * See http://stackoverflow.com/questions/2422468/how-to-upload-files-to-server-using-jsp-servlet/2424824#2424824
 */
@WebServlet("/servlets/employees/documents/upload")
@MultipartConfig
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EmployeeDocumentPostServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final int BUF_SIZE = 8192;
	
	private static final Marker LOG_TAG = MarkerManager.getMarker("BLOB");
	
    @Inject
	private Logger logger;
    
    @PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
    private EntityManager em;
        
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
    {
		System.err.println("DocumentPostServlet.doPost");
		
		String employeeIdString = request.getParameter("employeeId");
		long employeeId;
		try
		{
			employeeId = Long.parseLong(employeeIdString);
		}
		catch (NumberFormatException nfe)
		{
			if (logger != null)
				logger.warn(LOG_TAG, "Invalid employeeId " + employeeIdString + ". " + nfe.getMessage());
			
			response.sendError(Response.Status.BAD_REQUEST.getStatusCode(),
				"Invalid employeeId " + employeeIdString + ". " + nfe.getMessage());
			return;
		}
		String businessType = request.getParameter("businessType");		
		String mimeType = request.getParameter("mimeType");					
		Part filePart = request.getPart("file");
		
		//TODO: MIME Type check
		
		InputStream fileContent = filePart.getInputStream();		   
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		long bytesSize = readBlob(fileContent, out);
		
		EmployeeDocument document = new EmployeeDocument();
		Employee employee = em.find(Employee.class, employeeId);
		if (employee == null)
		{
			if (logger != null)
				logger.warn(LOG_TAG, "Employee " + employeeId + " not found!");
			response.sendError(Response.Status.NOT_FOUND.getStatusCode(), "Employee " + employeeId + " not found!");
			return;
		}
		
		document.setEmployee(employee);
		document.setBusinessType(businessType);
		document.setMimeType(mimeType);
		document.setDocumentBytesSize(bytesSize);
		document.setDocumentBytes(out.toByteArray());
				
		try
		{
			this.saveDocument(document);
			response.setHeader("Location", (new URI("./" + document.getOid()).toString()));
			response.setStatus(Status.CREATED.getStatusCode());
		}
		catch (URISyntaxException e)
		{
			if (logger != null)
				logger.warn(LOG_TAG, "Cannot build URI for created document!", e);
			response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage());
		}
    }
	
	@UserTransactional
	private void saveDocument(EmployeeDocument document)
	{
		em.persist(document);		
		if (logger != null && logger.isInfoEnabled())
			logger.info(LOG_TAG, "Document created with OID=" + document.getOid());
	}
	
	private static long readBlob(InputStream in, OutputStream out)
	{
		byte[] buffer = new byte[BUF_SIZE];
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
