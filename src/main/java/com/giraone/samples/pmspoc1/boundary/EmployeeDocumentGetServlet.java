package com.giraone.samples.pmspoc1.boundary;

import java.io.IOException;
import java.io.OutputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import com.giraone.samples.pmspoc1.entity.EmployeeDocument;

@WebServlet("/servlets/employees/document-download")
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class EmployeeDocumentGetServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	
    @PersistenceContext(unitName = PmsCoreApi.PERSISTENCE_UNIT)
    private EntityManager em;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
    {
		System.err.println("DocumentGetServlet.doGet");
		
		long documentId;		
		try
		{
			documentId = Long.parseLong(request.getParameter("documentId"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
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
		
		byte[] documentBytes = document.getDocumentBytes();
		
		response.setContentType(document.getMimeType());
		response.setContentLength((int) document.getDocumentBytesSize());
		OutputStream out = response.getOutputStream();
		long sendBytes = pipeBlob(out, documentBytes);
		
		final long end = System.currentTimeMillis();
		
		System.err.println("Document Bytes=" + sendBytes + ", Bytes/s=" + sendBytes / (0.001 * (end-start)));
    }
	
	private static long pipeBlob(OutputStream out, byte[] bytes)
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
