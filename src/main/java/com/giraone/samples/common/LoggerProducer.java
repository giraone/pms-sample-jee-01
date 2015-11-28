package com.giraone.samples.common;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Produce logger implementation (log4j2) to be injected by CDI. Simplified version of
 * <pre>
 * LogManager.getLogger(ClassName.class);
 * </pre>
 */
@ApplicationScoped
public class LoggerProducer
{	
	/**
	 * @param ip {@link InjectionPoint}
	 * @return {@link Logger} A logger instance for the declaring class (Injection Point Class)
	 */
	@Produces
	public Logger getLogger(final InjectionPoint ip)
	{
		return LogManager.getLogger(ip.getMember().getDeclaringClass());
	}
}
