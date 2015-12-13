package com.giraone.samples.pmspoc1.boundary.test;

import org.junit.Before;
import org.junit.BeforeClass;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.http.ContentType;

public class TestPmsCoreApi
{	
	static final String BASE_URI = "http://localhost/PmsSample";
	//static final String BASE_URI = "http://pmssamplejee01-giraone.rhcloud.com/PmsSample";
	//static final String BASE_URI = "http://pmssamplejee1.eu-gb.mybluemix.net";
	//static final String BASE_URI = "http://pmssamplejee1.cfapps.io";
	static final int PORT = 8080;
	//static final int PORT = 80;
	static final String PATH_API = "/api";
	
	static final int HTTP_UNPROCESSABLE = 422;
	
	RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
	
	@BeforeClass
	public static void setupConnection()
	{
		RestAssured.baseURI = BASE_URI;
		RestAssured.port = PORT;
		RestAssured.basePath = PATH_API;
	}

	@Before
	public void setup() throws Exception
	{
		requestSpecBuilder
			.setContentType(ContentType.JSON)
			.addHeader("Accept", ContentType.JSON.getAcceptHeader());
	}
}