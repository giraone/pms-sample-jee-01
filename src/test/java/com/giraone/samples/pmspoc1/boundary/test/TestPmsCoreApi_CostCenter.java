package com.giraone.samples.pmspoc1.boundary.test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.net.HttpURLConnection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.giraone.samples.pmspoc1.entity.CostCenter_;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/*
 * TODO: .body("name": "value") in POST/PUT ==> Encoder for value!
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPmsCoreApi_CostCenter extends TestPmsCoreApi
{
	static final String PATH_TO_RESOURCE = "/costcenters";
	
	static final String ENTITY_VALID_domainKey = "center12345";
	static final String ENTITY_NOT_EXISTING_domainKey = "fake98765";
	static final String ENTITY_INVALID_domainKey = "%()";
	
	static final String ENTITY_VALID_description = "Cost Center OneTwoThreeFourFive";
	
	
	@BeforeClass
	public static void setupConnection()
	{
		TestPmsCoreApi.setupConnection();
	}

	@Before
	public void setup() throws Exception
	{
		super.setup();
	}

	//------------------------------------------------------------------------------------------
	
	 /*
	  * Use this structure for tests:
	 	given()
	        .spec(requestSpecBuilder.build())
	        .pathParam("id", id) // if path parameter with name "id" is needed
	        .log().headers() // or .log().all()
	    .when()
	        .<get|post|put|delete>(PATH_TO_RESOURCE)
	        .<get|post|put|delete>(PATH_TO_RESOURCE + "/{id}")
	    .then()
	        .log().all() // or .log().body()
	        .statusCode(HttpURLConnection.HTTP_NOT_FOUND);
	        .contentType(ContentType.JSON); // if content is returned
	        .body("attr", is(attr_value)); // if response has to be checked
	        .body("any { it.key == 'attr' }", is(true)); // attr must be defined (no value check)
	        
	  * Samples JSON generation:
	  	JsonObject model = Json.createObjectBuilder()
		   .add("firstName", "Duke")
		   .add("lastName", "Java")
		   .add("age", 18)
		   .add("postalCode", "12345")
		   .add("phoneNumbers", Json.createArrayBuilder()
		      .add(Json.createObjectBuilder()
		         .add("type", "mobile")
		         .add("number", "111-111-1111"))
		      .add(Json.createObjectBuilder()
		         .add("type", "home")
		         .add("number", "222-222-2222")))
		   .build();
	  */
	
	//------------------------------------------------------------------------------------------
	
	@Test
	public void t_100_GET_byExistingId_shouldReturnCorrectStatusHeaderAndBody() throws Exception
	{	
		String domainKey = ENTITY_VALID_domainKey;
		int oid = this.createFreshEntityAndReturnOid(domainKey);
		
	    given()
	        .spec(requestSpecBuilder.build())
	        .pathParam("id", oid)
	        .log().all()
		.when()
			.get(PATH_TO_RESOURCE + "/{id}")
		.then()
			.log().body()
			.statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON)
			.body("oid", is(oid))
	    	.body("any { it.key == '" + CostCenter_.DTO_NAME_identification + "' }", is(true))
	    	.body("any { it.key == '" + CostCenter_.DTO_NAME_description + "' }", is(true));
	}
	
	@Test
	public void t_101_GET_byNonExistingId_shouldReturnStatusNotFound() throws Exception
	{
		// Create an entity, to get a valid oid ...
	    String domainKey = ENTITY_NOT_EXISTING_domainKey;
	    int oid = this.createFreshEntityAndReturnOid(domainKey);
	    // ... and delete it, to force "NOT FOUND"
	    this.deleteEntityByOidAndIgnoreStatus(oid);
		
	    given()
	        .spec(requestSpecBuilder.build())
	        .pathParam("id", oid)
	        .log().all()
		.when()
			.get(PATH_TO_RESOURCE + "/{id}")
		.then()
			.log().all()
			.statusCode(HttpURLConnection.HTTP_NOT_FOUND);
	}
	
	@Test
	public void t_110_GET_listAll_shouldReturnCorrectStatusAndHeader() throws Exception
	{	
	    given()
	        .spec(requestSpecBuilder.build())
		.when()
			.get(PATH_TO_RESOURCE)
		.then()
			.log().body()
			.statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON);
	}
		
	@Test
	public void t_111_GET_listAll_Top10_shouldReturnLessThanOrEqualTo() throws Exception
	{
		String response = given()
	        .spec(requestSpecBuilder.build())
	        .queryParam("top", 10)
	        .get(PATH_TO_RESOURCE).asString();
		int count = from(response).getList("").size();
		assertThat("count", count, lessThanOrEqualTo(10));
	}

	@Test
	public void t_112_GET_listAll_Skip1_shouldReturnNotFirst() throws Exception
	{
		String response1 = given()
	        .spec(requestSpecBuilder.build())
	        .queryParam("top", 10)
	        .queryParam("skip", 0)
	        .get(PATH_TO_RESOURCE).asString();
		long firstOid = from(response1).getLong("[0].oid");
		long secondOid = from(response1).getLong("[1].oid");
		assertThat(secondOid, not(equalTo(firstOid)));
		
		String response2 = given()
	        .spec(requestSpecBuilder.build())
	        .queryParam("top", 10)
	        .queryParam("skip", 1)
	        .get(PATH_TO_RESOURCE).asString();
		long firstOid2 = from(response2).getLong("[0].oid");
		assertThat(firstOid2, not(equalTo(secondOid)));
	}
	
	@Test
	public void t_200_POST_newValidData_shouldReturnStatusCreatedWithLocation() throws Exception
	{
		this.deleteEntityByIdentificationAndIgnoreStatus(ENTITY_VALID_domainKey);
		
		String jsonPayload = Json.createObjectBuilder()
		    .add(CostCenter_.DTO_NAME_identification, ENTITY_VALID_domainKey)
		    .add(CostCenter_.DTO_NAME_description, ENTITY_VALID_description)
		    .build()
		    .toString();
		
		Response response = given()
	        .spec(requestSpecBuilder.build())
	        .body(jsonPayload)
	    .when()
	        .post(PATH_TO_RESOURCE)
	    .then()
	        .statusCode(HttpURLConnection.HTTP_CREATED)
	        .headers("location", containsString(PATH_TO_RESOURCE + "/"))
	        .and().extract().response();
	    
	    // Now get header location, use it and check the values:
		String getUri = response.header("location");
		given()
        	.spec(requestSpecBuilder.build())
        .when()
        	.get(getUri)
        .then()
			.log().body()
			.statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON)	        
	        .body(CostCenter_.DTO_NAME_identification, is(ENTITY_VALID_domainKey))
	        .body(CostCenter_.DTO_NAME_description, is(ENTITY_VALID_description));
	}
	
	@Test
	public void t_200_POST_duplicateValidData_shouldReturnStatusConflict() throws Exception
	{		
		this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);
		
		String jsonPayload = Json.createObjectBuilder()
		    .add(CostCenter_.DTO_NAME_identification, ENTITY_VALID_domainKey)
		    .add(CostCenter_.DTO_NAME_description, ENTITY_VALID_description)
		    .build()
		    .toString();
		
	    given()
	        .spec(requestSpecBuilder.build())
	        .body(jsonPayload)
	    .when()
	        .post(PATH_TO_RESOURCE)
	    .then()
	    	.log().all()
	        .statusCode(HttpURLConnection.HTTP_CONFLICT);
	}
	
	@Test
	public void t_300_PUT_validData_shouldReturnStatusNoContent() throws Exception
	{
	    int oid = this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);
	    String getUri = PATH_TO_RESOURCE + "/" + oid;
	    Response oldResponse = given().spec(requestSpecBuilder.build()).get(getUri);
	    int oldVersionNumber = oldResponse.path(CostCenter_.DTO_NAME_versionNumber);
	    String oldDomainKey = oldResponse.path(CostCenter_.DTO_NAME_identification);
	    
	    String newDescription = "newDescription";
	    
	    ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
	    noContentInResponse.expectBody(is("")).expectContentType("");
	     
		String jsonPayload = Json.createObjectBuilder()
		    .add(CostCenter_.DTO_NAME_oid, oid)
		    .add(CostCenter_.DTO_NAME_identification, ENTITY_VALID_domainKey)
		    .add(CostCenter_.DTO_NAME_description, newDescription)
		    .build()
		    .toString();
		
	    given()
	        .spec(requestSpecBuilder.build())
	        .body(jsonPayload)
	        .pathParam("id", oid)
	    .when()
	        .put(PATH_TO_RESOURCE + "/{id}")
	    .then()
	        .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
	        .spec(noContentInResponse.build());
	    	    
	    // Now use the GET URI again and check the new values:
		given()
        	.spec(requestSpecBuilder.build())
        .when()
        	.get(getUri)
        .then()
			.statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON)
			.body(CostCenter_.DTO_NAME_oid, is(oid))
			.body(CostCenter_.DTO_NAME_versionNumber, greaterThan(oldVersionNumber))
	        .body(CostCenter_.DTO_NAME_identification, is(oldDomainKey))
	        .body(CostCenter_.DTO_NAME_description, is(newDescription));
	}

	@Test
	public void t_300_PUT_orginalData_shouldReturnStatusNoContentAndNoVersionChange() throws Exception
	{
	    int oid = this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);
	    String getUri = PATH_TO_RESOURCE + "/" + oid;
	    Response oldResponse = given().spec(requestSpecBuilder.build()).get(getUri);
	    int oldVersionNumber = oldResponse.path(CostCenter_.DTO_NAME_versionNumber);
	    String oldDomainKey = oldResponse.path(CostCenter_.DTO_NAME_identification);
	    String oldDescription = oldResponse.path(CostCenter_.DTO_NAME_description);
	    
	    JsonReader reader = Json.createReader(new StringReader(oldResponse.asString()));      
        JsonObject oldJsonObject = reader.readObject();       
        //oldJsonObject.put(CostCenter_.DTO_NAME_description, "newDescription2");
	    
	    ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
	    noContentInResponse.expectBody(is("")).expectContentType("");
	     
		String jsonPayload = oldJsonObject.toString();
		
	    given()
	        .spec(requestSpecBuilder.build())
	        .body(jsonPayload)
	        .pathParam("id", oid)
	    .when()
	        .put(PATH_TO_RESOURCE + "/{id}")
	    .then()
	        .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
	        .spec(noContentInResponse.build());
	    	    
	    // Now use the GET URI again and check the new values:
		given()
        	.spec(requestSpecBuilder.build())
        .when()
        	.get(getUri)
        .then()
			.statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON)
			.body(CostCenter_.DTO_NAME_oid, is(oid))
			.body(CostCenter_.DTO_NAME_versionNumber, is(oldVersionNumber))
	        .body(CostCenter_.DTO_NAME_identification, is(oldDomainKey))
	        .body(CostCenter_.DTO_NAME_description, is(oldDescription));
	}
	
	@Test
	public void t_400_DELETE_existing_shouldReturnStatusNoContent() throws Exception
	{
		int newOid = this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);
	     
	    ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
	    noContentInResponse.expectBody(is("")).expectContentType("");
	    
	    given()
	        .spec(requestSpecBuilder.build())
	        .pathParam("id", newOid)
	        .log().headers()
	    .when()
	        .delete(PATH_TO_RESOURCE + "/{id}")
	    .then()
	        .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
        	.spec(noContentInResponse.build());
	}
	
	@Test
	public void t_401_DELETE_nonExisting_shouldReturnStatusNotFound() throws Exception
	{
		// Create an entity, to get a valid oid ...
	    String domainKey = ENTITY_NOT_EXISTING_domainKey;
	    int oid = this.createFreshEntityAndReturnOid(domainKey);
	    // ... and delete it, to force "NOT FOUND"
	    this.deleteEntityByOidAndIgnoreStatus(oid);
	    
	    given()
	        .spec(requestSpecBuilder.build())
	        .pathParam("id", oid)
	        .log().headers()
	    .when()
	        .delete(PATH_TO_RESOURCE + "/{id}")
	    .then()
	        .statusCode(HttpURLConnection.HTTP_NOT_FOUND);
	}
		
	//------------------------------------------------------------------------------------------

	private void deleteEntityByOidAndIgnoreStatus(int oid)
	{		
	    given()
	        .spec(requestSpecBuilder.build())
	        .pathParam("id", oid)
	        .delete(PATH_TO_RESOURCE + "/{id}");
	}
	
	private void deleteEntityByIdentificationAndIgnoreStatus(String domainKey)
	{
		Response response = given()
        	.spec(requestSpecBuilder.build())
        	.pathParam("domainKey", domainKey)
        	.get(PATH_TO_RESOURCE + "/id-{domainKey}");
		
		if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND)
		{
			return;
		}
		
		int oid;
		try
		{
			oid = from(response.body().asString()).getInt("oid");
		}
		catch (Exception e)
		{
			System.err.println("----- Invalid body is  -----");
	    	System.err.println(response.body().asString());
	    	System.err.println("----- Invalid body end -----");
			throw e;
		}
		this.deleteEntityByOidAndIgnoreStatus(oid);
	}
	
	int createFreshEntityAndReturnOid(String domainKey)
	{	
		this.deleteEntityByIdentificationAndIgnoreStatus(domainKey);		
	    Response response = given()
	    	.spec(requestSpecBuilder.build())
	        	.body("{"
		        	+ "\"" + CostCenter_.DTO_NAME_identification + "\":\"" + domainKey + "\","
		        	+ "\"" + CostCenter_.DTO_NAME_description + "\":\"Description for " + domainKey + "\""
		        	+ "}")
	    		.post(PATH_TO_RESOURCE);
	    
	    String entityLocation = response.header("location");
	    if (entityLocation == null)
	    {
	    	System.err.println("WARNING: No location header in HTTP POST response!");
	    	System.err.println("----- Body is  -----");
	    	System.err.println(response.body().asString());
	    	System.err.println("----- Body end -----");
	    	throw new IllegalStateException("No location header in HTTP POST response!");
	    }

    	return Integer.parseInt(entityLocation.substring(
    		entityLocation.lastIndexOf("/") + 1, entityLocation.length()));
	}
}