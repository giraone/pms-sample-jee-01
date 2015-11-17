package com.giraone.samples.pmspoc1.boundary.core;

import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * TODO: Only for non-production!
 */
public class BaseEndpoint
{
	protected final static String DEFAULT_PAGING_SIZE = "20";
	
	// Matches root-resources also - handle CORS pre-flight request
	@OPTIONS
	public Response options()
	{
	    return Response.status(Response.Status.NO_CONTENT)
	    	.header("Access-Control-Allow-Origin", "*") // "127.0.0.1" does not work
	    	//.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
			.header("Access-Control-Allow-Methods", "*")
	    	.build();
	}
	
	// Match sub-resources - handle CORS pre-flight request
	@OPTIONS
	@Path("{path:.*}")
	public Response optionsAll(@PathParam("path") String path)
	{
	    return Response.status(Response.Status.NO_CONTENT)
	    	.header("Access-Control-Allow-Origin", "*") // "127.0.0.1" does not work
			.header("Access-Control-Allow-Methods", "*")
			.build();
	}
	
	// No more used - see CorsFeature class
	public Response buildWithCorsHeader(ResponseBuilder builder)
	{	        
		builder.header("Access-Control-Allow-Origin", "*");     
        return builder.build();
	}
}