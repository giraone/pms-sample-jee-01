package com.giraone.samples.common.boundary;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
// import javax.ws.rs.core.Configurable; // JEE6 - JAX-RS 1.1 ONLY!
import javax.ws.rs.core.Feature; // JEE7 - JAX-RS 2.0 ONLY!
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * A response filter for JAX/RS to configure CORS to allow requests from any host.
 */
@Provider
public class CorsFeature implements Feature, ContainerResponseFilter
{
	private static final Marker LOG_TAG = MarkerManager.getMarker("API-CORS");
	
	private static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	private static final String CORS_ALLOW_ORIGIN_VALUE = "*"; // "127.0.0.1" does not work
	private static final String CORS_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
	private static final String CORS_ALLOW_HEADERS_VALUE = "content-length,content-type,location";
	private static final String CORS_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
	private static final String CORS_EXPOSE_HEADERS_VALUE = "location";
		
	@Inject
	private Logger logger;

	@Override
	public boolean configure(FeatureContext context)
	{
		return true;
	}

	/*
	// JEE6 - JAX-RS 1.2 version
	@Override
	public boolean configure(Configurable context)
	{
		return true;
	}
	*/
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
		throws IOException
	{
		final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		if (!headers.containsKey(CORS_ALLOW_ORIGIN_HEADER))
		{
			headers.add(CORS_ALLOW_ORIGIN_HEADER, CORS_ALLOW_ORIGIN_VALUE);
		}
		if (!headers.containsKey(CORS_ALLOW_HEADERS_HEADER))
		{
			headers.add(CORS_ALLOW_HEADERS_HEADER, CORS_ALLOW_HEADERS_VALUE);
		}
		if (!headers.containsKey(CORS_EXPOSE_HEADERS_HEADER))
		{
			headers.add(CORS_EXPOSE_HEADERS_HEADER, CORS_EXPOSE_HEADERS_VALUE);
		}
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, CORS_ALLOW_ORIGIN_HEADER + "=" + headers.get(CORS_ALLOW_ORIGIN_HEADER));
		}
	}
}
