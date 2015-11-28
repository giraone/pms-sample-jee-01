package com.giraone.samples.common.boundary;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
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
	private static final String CORS_ALLOW_HEADER = "Access-Control-Allow-Origin";
	
	@Inject
	private Logger logger;

	@Override
	public boolean configure(FeatureContext context)
	{
		return true;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
		throws IOException
	{
		final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		if (!headers.containsKey(CORS_ALLOW_HEADER))
		{
			headers.add(CORS_ALLOW_HEADER, "*"); // "127.0.0.1" may not work always
		}
		if (logger != null && logger.isDebugEnabled())
		{
			logger.debug(LOG_TAG, "Access-Control-Allow-Origin=" + headers.get("Access-Control-Allow-Origin"));
		}
	}
}
