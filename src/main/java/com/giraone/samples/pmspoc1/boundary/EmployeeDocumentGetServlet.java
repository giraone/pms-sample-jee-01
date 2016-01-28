package com.giraone.samples.pmspoc1.boundary;

import java.io.IOException;
import java.io.OutputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.giraone.samples.pmspoc1.entity.EmployeeDocument;

@WebServlet("/servlets/employees/documents/download")
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class EmployeeDocumentGetServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static final Marker LOG_TAG = MarkerManager.getMarker("BLOB");
	
    @Inject
	private Logger logger;
    
    @PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
    private EntityManager em;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
    {
		String employeeIdString = request.getParameter("employeeId");
		String documentIdString = request.getParameter("documentId");
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "EmployeeDocumentGetServlet.doGet employeeId=" + employeeIdString);
			logger.debug(LOG_TAG, "EmployeeDocumentGetServlet.doGet documentId=" + documentIdString);
		}
		
		long employeeId, documentId;
		try
		{
			employeeId = Long.parseLong(employeeIdString);
		}
		catch (NumberFormatException nfe)
		{
			if (logger != null)
			{
				logger.warn(LOG_TAG, "Invalid employeeId " + employeeIdString + ". " + nfe.getMessage());
			}
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try
		{
			documentId = Long.parseLong(documentIdString);
		}
		catch (NumberFormatException nfe)
		{
			if (logger != null)
			{
				logger.warn(LOG_TAG, "Invalid documentId " + documentIdString + ". " + nfe.getMessage());
			}
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		EmployeeDocument document = em.find(EmployeeDocument.class, documentId);
		if (document == null)
		{
			response.sendError(Response.Status.NOT_FOUND.getStatusCode(), "EmployeeDocument " + documentId + " not found!");
			return;
		}
						
		final long start = System.currentTimeMillis();
		
		// TODO: Version without bytes in memory
		byte[] documentBytes = document.getDocumentBytes();
		
		response.setContentType(document.getMimeType());
		response.setContentLength((int) document.getByteSize());
		OutputStream out = response.getOutputStream();
		long sendBytes = streamBlob(out, documentBytes);
		
		final long end = System.currentTimeMillis();
		
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "Document Bytes=" + sendBytes + ", Bytes/s=" + (sendBytes / (end-start)));
		}
    }
	
	private static long streamBlob(OutputStream out, byte[] bytes)
	{
        try
		{
			out.write(bytes, 0, bytes.length);
			out.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -1;
		}
        
        return bytes.length;
	}
}
