package com.giraone.samples.common.boundary;

import javax.inject.Inject;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Base class for REST services. Features:
 * <ul>
 * <li>Adds support for CORSpre-flight requests.</li>
 * <li>Adds support for logging of all REST calls.</li>
 * <li>Adds support for throttling feature</li>
 * </ul>
 * TODO: CORS only for non-production!
 */
public class BaseEndpoint
{
	protected static final Marker LOG_TAG = MarkerManager.getMarker("API");

	protected final static String DEFAULT_PAGING_SIZE = "20";

	private static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	private static final String CORS_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
	private static final String CORS_ALLOW_REQUEST_HEADER = "Access-Control-Allow-Headers";

	private static final int THROTTLE_MSEC = 1000;

	@Inject
	protected Logger logger;

	// Matches root-resources also - handle CORS pre-flight request
	@OPTIONS
	public Response options()
	{
		return Response.status(Response.Status.NO_CONTENT).header(CORS_ALLOW_ORIGIN_HEADER, "*") // "127.0.0.1" does not
																									// work
			.header(CORS_ALLOW_METHODS_HEADER, "GET, POST, DELETE, PUT, OPTIONS") // * THIS DOES NOT WORK HERE!
			.header(CORS_ALLOW_REQUEST_HEADER, "content-type").build();
	}

	// Match sub-resources - handle CORS pre-flight request
	@OPTIONS
	@Path("{path:.*}")
	public Response optionsAll(@PathParam("path") String path)
	{
		return Response.status(Response.Status.NO_CONTENT).header(CORS_ALLOW_ORIGIN_HEADER, "*") // "127.0.0.1" does not
																									// work
			.header(CORS_ALLOW_METHODS_HEADER, "GET, POST, DELETE, PUT, OPTIONS") // * THIS DOES NOT WORK HERE!
			.header(CORS_ALLOW_REQUEST_HEADER, "content-type").build();
	}

	//@AroundInvoke
	/*
	private Object doInterceptJaxRsMethods(InvocationContext invocationContext) throws Exception
	{
		final Method method = invocationContext.getMethod();
		// check if JAX-RS annotation is present
		if (method.getAnnotation(Produces.class) == null && method.getAnnotation(Consumes.class) == null)
		{
			return invocationContext.proceed();
		}

		try
		{
			if (THROTTLE_MSEC > 0)
				Thread.sleep(THROTTLE_MSEC);
		}
		catch (InterruptedException ignore)
		{
		}

		Object obj = null;
		try
		{
			if (logger != null && logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG, "Entering: " + invocationContext.getTarget().getClass().getSimpleName() + "."
					+ invocationContext.getMethod().getName());
				
				StringBuilder sb = null;
				for (Object param : invocationContext.getParameters())
				{
					if (sb == null)
					{
						sb = new StringBuilder();
						sb.append(param);
					}
					else
					{
						sb.append(", ").append(param);
					}
				}
				logger.debug(LOG_TAG, "Params:   " + sb == null ? "null" : sb.toString());
				
			}
			obj = invocationContext.proceed();
		}
		finally
		{
			if (logger != null && logger.isDebugEnabled())
			{
				logger.debug(LOG_TAG, "Leaving:  " + invocationContext.getTarget().getClass().getSimpleName() + "."
					+ invocationContext.getMethod().getName());
			}
		}

		return obj;
	}
	*/
}