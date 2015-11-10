package com.giraone.samples.common;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class LoggerProducer
{	
	/**
	 * @param ip {@link InjectionPoint}
	 * @return {@link Logger} Instance of declaring class (Injection Point Class)
	 */
	@Produces
	public Logger getLogger(final InjectionPoint ip)
	{
		return LogManager.getLogger(ip.getMember().getDeclaringClass());
	}
}
