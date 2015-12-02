package com.giraone.samples.pmspoc1.boundary;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
public class PmsCoreApi extends Application
{
	public final static String PERSISTENCE_UNIT = "primary";
}