package com.giraone.samples.pmspoc1.boundary;

import javax.ws.rs.core.Application;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class PmsCoreApi extends Application
{
	public final static String PERSISTENCE_UNIT = "persistence-unit";
}