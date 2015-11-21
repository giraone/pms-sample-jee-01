package com.giraone.samples.pmspoc1.boundary.core;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * TODO: Only for non-production!
 */
public class BaseEndpoint
{
	protected final static String DEFAULT_PAGING_SIZE = "20";
	private static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	private static final String CORS_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
	private static final String CORS_ALLOW_REQUEST_HEADER = "Access-Control-Allow-Headers";
	
	// Matches root-resources also - handle CORS pre-flight request
	@OPTIONS
	public Response options()
	{
	    return Response.status(Response.Status.NO_CONTENT)
	    	.header(CORS_ALLOW_ORIGIN_HEADER, "*") // "127.0.0.1" does not work
	    	//.header(CORS_ALLOW_METHODS_HEADER, "GET, POST, DELETE, PUT, OPTIONS")
			.header(CORS_ALLOW_METHODS_HEADER, "*")
			.header(CORS_ALLOW_REQUEST_HEADER, "content-type")			
	    	.build();
	}
	
	// Match sub-resources - handle CORS pre-flight request
	@OPTIONS
	@Path("{path:.*}")
	public Response optionsAll(@PathParam("path") String path)
	{
	    return Response.status(Response.Status.NO_CONTENT)
	    	.header(CORS_ALLOW_ORIGIN_HEADER, "*") // "127.0.0.1" does not work
			.header(CORS_ALLOW_METHODS_HEADER, "*")
			.header(CORS_ALLOW_REQUEST_HEADER, "content-type")			
			.build();
	}
	
	// No more used - see CorsFeature class
	/*
	public Response buildWithCorsHeader(ResponseBuilder builder)
	{	        
		builder.header("Access-Control-Allow-Origin", "*");     
        return builder.build();
	}
	*/
}