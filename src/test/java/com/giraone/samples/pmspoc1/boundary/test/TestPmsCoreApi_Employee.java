package com.giraone.samples.pmspoc1.boundary.test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Random;

import javax.json.Json;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.giraone.samples.pmspoc1.entity.CostCenter_;
import com.giraone.samples.pmspoc1.entity.Employee_;
import com.giraone.samples.pmspoc1.entity.enums.EnumGender;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/*
 * TODO: .body("name": "value") in POST/PUT ==> Encoder for value!
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPmsCoreApi_Employee extends TestPmsCoreApi
{
	static final String PATH_TO_RESOURCE = "/employees";

	static final String ENTITY_VALID_domainKey = "123456";
	static final String ENTITY_NOT_EXISTING_domainKey = "fake98765";

	static final Random RANDOM = new Random();
	
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

	// ------------------------------------------------------------------------------------------

	/*
	 * Use this structure for tests: given() .spec(requestSpecBuilder.build()) .pathParam("id", id) // if path parameter
	 * with name "id" is needed .log().headers() // or .log().all() .when() .<get|post|put|delete>(PATH_TO_RESOURCE)
	 * .<get|post|put|delete>(PATH_TO_RESOURCE + "/{id}") .then() .log().all() // or .log().body()
	 * .statusCode(HttpURLConnection.HTTP_NOT_FOUND); .contentType(ContentType.JSON); // if content is returned
	 * .body("attr", is(attr_value)); // if response has to be checked
	 * 
	 * Samples JSON generation: JsonObject model = Json.createObjectBuilder() .add("firstName", "Duke") .add("lastName",
	 * "Java") .add("age", 18) .add("postalCode", "12345") .add("phoneNumbers", Json.createArrayBuilder()
	 * .add(Json.createObjectBuilder() .add("type", "mobile") .add("number", "111-111-1111"))
	 * .add(Json.createObjectBuilder() .add("type", "home") .add("number", "222-222-2222"))) .build();
	 */

	// ------------------------------------------------------------------------------------------

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
	public void t_111_GET_listAll_top10_shouldReturnLessThanOrEqualTo() throws Exception
	{
		int top = 3;
		String response = given()
	        .spec(requestSpecBuilder.build())
	        .queryParam("top", top)
	        .get(PATH_TO_RESOURCE).asString();
		int count = from(response).getList("").size();
		assertThat("count", count, lessThanOrEqualTo(top));
	}

	@Test
	public void t_112_GET_listAll_skip1_shouldReturnNotFirst() throws Exception
	{
		String response1 = given()
	        .spec(requestSpecBuilder.build())
	        .queryParam("top", 10)
	        .queryParam("skip", 0)
	        .get(PATH_TO_RESOURCE).asString();
		int count = from(response1).getList("").size();
		if (count < 3) return;
		
		long firstOid = from(response1).getLong("[0].oid");
		long secondOid = from(response1).getLong("[1].oid");
		assertThat(secondOid, not(equalTo(firstOid)));
		
		String response2 = given()
	        .spec(requestSpecBuilder.build())
	        .queryParam("top", 10)
	        .queryParam("skip", 1)
	        .get(PATH_TO_RESOURCE).asString();
		long firstOid2 = from(response2).getLong("[0].oid");
		assertThat(firstOid2, equalTo(secondOid));
	}

	@Test
	public void t_113_GET_listAll_filters_shouldWork() throws Exception
	{
		// Create an entity, to get a valid oid ...
		int oid = this.createFreshEntityAndReturnOid();
		String getUri = PATH_TO_RESOURCE + "/" + oid;
		Response response1 = given().spec(requestSpecBuilder.build()).get(getUri);
		String personnelNumber = response1.path(Employee_.DTO_NAME_personnelNumber);
		String lastName = response1.path(Employee_.DTO_NAME_lastName);
		//String dateOfBirth = response1.path(Employee_.DTO_NAME_dateOfBirth);
		
		{
			String response = given()
		        .spec(requestSpecBuilder.build())
		        .queryParam("filter", CostCenter_.DTO_NAME_oid + " eq " + oid)
				.get(PATH_TO_RESOURCE).asString();
			int fetchedOid = from(response).getInt("[0].oid");
			assertThat(fetchedOid, equalTo(oid));
		}
		
		{
			String response = given()
		        .spec(requestSpecBuilder.build())
		        .queryParam("filter", Employee_.DTO_NAME_personnelNumber + " eq '" + personnelNumber + "'")
				.get(PATH_TO_RESOURCE).asString();
			int fetchedOid = from(response).getInt("[0].oid");
			assertThat(fetchedOid, equalTo(oid));
		}
		
		{
			String response = given()
		        .spec(requestSpecBuilder.build())
		        .queryParam("filter", Employee_.DTO_NAME_oid + " eq " + oid + " and " + Employee_.DTO_NAME_personnelNumber + " eq '" + personnelNumber + "'")
				.get(PATH_TO_RESOURCE).asString();
			int fetchedOid = from(response).getInt("[0].oid");
			assertThat(fetchedOid, equalTo(oid));
		}
		
		{
			String response = given()
		        .spec(requestSpecBuilder.build())
		        .queryParam("filter", Employee_.DTO_NAME_lastName + " eq " + lastName)
				.get(PATH_TO_RESOURCE).asString();
			int fetchedOid = from(response).getInt("[0].oid");
			assertThat(fetchedOid, equalTo(oid));
		}
	}
	
	@Test
	public void t_100_GET_simple_shouldReturnCorrectStatusAndHeader() throws Exception
	{
		given().spec(requestSpecBuilder.build()).log().all().when().get(PATH_TO_RESOURCE).then().log().body()
			.statusCode(HttpURLConnection.HTTP_OK).contentType(ContentType.JSON);
	}

	@Test
	public void t_101_GET_byExistingId_shouldReturnCorrectStatusHeaderAndBody() throws Exception
	{
		String domainKey = ENTITY_VALID_domainKey;
		int oid = this.createFreshEntityAndReturnOid(domainKey);

		given().spec(requestSpecBuilder.build()).pathParam("id", oid).log().all().when().get(PATH_TO_RESOURCE + "/{id}")
			.then().log().body().statusCode(HttpURLConnection.HTTP_OK).contentType(ContentType.JSON)
			.body("oid", is(oid));
	}

	@Test
	public void t_102_GET_byNonExistingId_shouldReturnStatusNotFound() throws Exception
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
	public void t_200_POST_newValidData_shouldReturnStatusCreatedWithLocation() throws Exception
	{
		this.deleteEntityByIdentificationAndIgnoreStatus(ENTITY_VALID_domainKey);
		
		long costCenterId = this.createFreshCostCenterAndReturnOid("CO1234");
		
		String jsonPayload = Json.createObjectBuilder()
			.add(Employee_.DTO_NAME_costCenter, Json.createObjectBuilder()
				.add(CostCenter_.DTO_NAME_oid, costCenterId))
			.add(Employee_.DTO_NAME_personnelNumber, ENTITY_VALID_domainKey)
			.add(Employee_.DTO_NAME_lastName, "Doe")
			.add(Employee_.DTO_NAME_firstName, "Jane")
			.add(Employee_.DTO_NAME_dateOfBirth, "1966-12-31T00:00:00.000Z")
			.add(Employee_.DTO_NAME_gender, EnumGender.F.toString())
			.add(Employee_.DTO_NAME_dateOfEntry, "2014-01-01T00:00:00.000Z")
			.add(Employee_.DTO_NAME_nationalityCode, "DEU")
			.build().toString();

		System.err.println("######## " + jsonPayload);

		Response response =
			given()
				.spec(requestSpecBuilder.build())
				.body(jsonPayload)
				.log().all()
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
			.body(Employee_.DTO_NAME_personnelNumber, is(ENTITY_VALID_domainKey))
			.body(Employee_.DTO_NAME_lastName, is("Doe"));
	}

	@Test
	public void t_200_POST_newValidDataWithDateLong_shouldReturnStatusCreatedWithLocation() throws Exception
	{
		this.deleteEntityByIdentificationAndIgnoreStatus(ENTITY_VALID_domainKey);
		
		long costCenterId = this.createFreshCostCenterAndReturnOid("CO1234");
		
		String jsonPayload = Json.createObjectBuilder()
			.add(Employee_.DTO_NAME_costCenter, Json.createObjectBuilder()
				.add(CostCenter_.DTO_NAME_oid, costCenterId))
			.add(Employee_.DTO_NAME_personnelNumber, ENTITY_VALID_domainKey)
			.add(Employee_.DTO_NAME_lastName, "Date")
			.add(Employee_.DTO_NAME_firstName, "John")
			.add(Employee_.DTO_NAME_dateOfBirth, (new Date()).getTime() - 3600*24*365*40)
			.add(Employee_.DTO_NAME_gender, EnumGender.M.toString())
			.add(Employee_.DTO_NAME_dateOfEntry, (new Date()).getTime() - 3600*24*365*2)
			.add(Employee_.DTO_NAME_nationalityCode, "DEU")
			.build().toString();

		System.err.println("######## " + jsonPayload);

		Response response =
			given()
				.spec(requestSpecBuilder.build())
				.body(jsonPayload)
				.log().all()
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
			.body(Employee_.DTO_NAME_personnelNumber, is(ENTITY_VALID_domainKey))
			.body(Employee_.DTO_NAME_lastName, is("Doe"));
	}
	
	/*
	@Test
	public void t_200_POST_duplicateValidData_shouldReturnStatusConflict() throws Exception
	{
		this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);

		given().spec(requestSpecBuilder.build())
			.body("{" + "\"" + Employee_.DTO_NAME_personnelNumber + "\":\"" + ENTITY_VALID_domainKey + "\"," + "\""
				+ Employee_.DTO_NAME_lastName + "\":\"" + "Doe" + "\"" + "}")
			.when().post(PATH_TO_RESOURCE).then().log().all().statusCode(HttpURLConnection.HTTP_CONFLICT);
	}
	*/
	
	@Test
	public void t_300_PUT_validData_shouldReturnStatusNoContent() throws Exception
	{
		int oid = this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);
		String getUri = PATH_TO_RESOURCE + "/" + oid;
		Response oldResponse = given().spec(requestSpecBuilder.build()).get(getUri);
		int oldVersionNumber = oldResponse.path(Employee_.DTO_NAME_versionNumber);
		String oldPersonnelNumber = oldResponse.path(Employee_.DTO_NAME_personnelNumber);

		String newLastName = "Miller";

		ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
		noContentInResponse.expectBody(is("")).expectContentType("");

		given().spec(requestSpecBuilder.build())
			.body("{" + "\"" + Employee_.DTO_NAME_oid + "\":" + oid + "," + "\"" + Employee_.DTO_NAME_personnelNumber
				+ "\":\"" + oldPersonnelNumber + "\"," + "\"" + Employee_.DTO_NAME_lastName + "\":\"" + newLastName
				+ "\"" + "}")
			.pathParam("id", oid).when().put(PATH_TO_RESOURCE + "/{id}").then()
			.statusCode(HttpURLConnection.HTTP_NO_CONTENT).spec(noContentInResponse.build());

		// Now use the GET URI again and check the new values:
		given().spec(requestSpecBuilder.build()).when().get(getUri).then().statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON).body(Employee_.DTO_NAME_oid, is(oid))
			.body(Employee_.DTO_NAME_versionNumber, greaterThan(oldVersionNumber))
			.body(Employee_.DTO_NAME_personnelNumber, is(oldPersonnelNumber))
			.body(Employee_.DTO_NAME_lastName, is(newLastName));
	}

	@Test
	public void t_400_DELETE_existing_shouldReturnStatusNoContent() throws Exception
	{
		int newOid = this.createFreshEntityAndReturnOid(ENTITY_VALID_domainKey);

		ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
		noContentInResponse.expectBody(is("")).expectContentType("");

		given().spec(requestSpecBuilder.build()).pathParam("id", newOid).log().headers().when()
			.delete(PATH_TO_RESOURCE + "/{id}").then().statusCode(HttpURLConnection.HTTP_NO_CONTENT)
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

		given().spec(requestSpecBuilder.build()).pathParam("id", oid).log().headers().when()
			.delete(PATH_TO_RESOURCE + "/{id}").then().statusCode(HttpURLConnection.HTTP_NOT_FOUND);
	}

	// ------------------------------------------------------------------------------------------

	private void deleteEntityByOidAndIgnoreStatus(int oid)
	{
		given().spec(requestSpecBuilder.build()).pathParam("id", oid).delete(PATH_TO_RESOURCE + "/{id}");
	}

	private void deleteEntityByIdentificationAndIgnoreStatus(String domainKey)
	{
		Response response = given()
			.spec(requestSpecBuilder.build())
			.pathParam("domainKey", domainKey)
			.get(PATH_TO_RESOURCE + "/pnr-{domainKey}");

		if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND)
		{
			System.err.println("deleteEntityByIdentificationAndIgnoreStatus NOT_FOUND: " + domainKey);
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
		System.err.println("deleteEntityByIdentificationAndIgnoreStatus TO_DELETE: " + domainKey);
		this.deleteEntityByOidAndIgnoreStatus(oid);
	}

	private int createFreshEntityAndReturnOid(String domainKey)
	{
		this.deleteEntityByIdentificationAndIgnoreStatus(domainKey);
		
		Response response = given()
			.spec(requestSpecBuilder.build())
			.body("{"
				+ "\"" + Employee_.DTO_NAME_personnelNumber + "\":\"" + domainKey
			    + "\"," + "\"" + Employee_.DTO_NAME_lastName + "\":\"LastName-" + domainKey + "\"" + "}")
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

	int createFreshEntityAndReturnOid()
	{
		return this.createFreshEntityAndReturnOid("R" + RANDOM.nextInt(100000));
	}
	
	private int createFreshCostCenterAndReturnOid(String domainKey)
	{
		TestPmsCoreApi_CostCenter costCenterTest = new TestPmsCoreApi_CostCenter();
		try
		{
			TestPmsCoreApi_CostCenter.setupConnection();
			costCenterTest.setup();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return costCenterTest.createFreshEntityAndReturnOid(domainKey);
	}
}