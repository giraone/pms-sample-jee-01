package com.giraone.samples.common.boundary;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.giraone.samples.common.StringUtil;

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
	protected static final int HTTP_UNPROCESSABLE = 422;

	protected static final Marker LOG_TAG = MarkerManager.getMarker("API");

	protected final static String DEFAULT_PAGING_SIZE = "20";

	private static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
	private static final String CORS_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
	private static final String CORS_ALLOW_REQUEST_HEADER = "Access-Control-Allow-Headers";

	private static final int THROTTLE_MSEC = 0; // 1000;

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

	@AroundInvoke
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
				
				// Logs all parameters with their annotations
				StringBuilder sb = new StringBuilder();
				Annotation[][] annotations = method.getParameterAnnotations();
				Object[] params = invocationContext.getParameters();
				for (int i = 0; i < params.length; i++)
				{
					if (i > 0)
					{
						sb.append(", ");
					}
					if (annotations[i] != null && annotations[i].length > 0)
					{
						sb.append(annotations[i][0]);
					}
					else
					{
						sb.append(i);
					}
					sb.append("=");
					Object param = params[i];
					if (param instanceof String)
						sb.append(StringUtil.serializeAsJavaString((String) param));
					else
						sb.append(param);
				}
				logger.debug(LOG_TAG, "  Params: " + (sb == null ? "null" : sb.toString()));				
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
}