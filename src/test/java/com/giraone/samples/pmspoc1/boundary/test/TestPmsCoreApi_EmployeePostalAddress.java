package com.giraone.samples.pmspoc1.boundary.test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.net.HttpURLConnection;

import javax.json.Json;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress_;
import com.giraone.samples.pmspoc1.entity.Employee_;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

@FixMethodOrder(NAME_ASCENDING)
public class TestPmsCoreApi_EmployeePostalAddress extends TestPmsCoreApi
{
	static final String PATH_TO_EMPLOYEE_RESOURCE = "/employees";
	static final String PATH_TO_ADDRESS_RESOURCE = "/addresses";
	
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

	@Test
	public void t_200_POST_addSinglePostalAddress_ShouldWork() throws Exception
	{	
		//long employeeId = this.getEmployeeOidByIdentification("00000");
		long employeeId = this.createFreshEmployeeAndReturnOid();
		this.addSinglePostalAddress(employeeId, 1);
		
		String response = given()
	        .spec(requestSpecBuilder.build())
	        .get(PATH_TO_EMPLOYEE_RESOURCE + "/" + employeeId + PATH_TO_ADDRESS_RESOURCE)
	        .asString();
		int count = from(response).getList("").size();
		assertThat(count, equalTo(1));
	}

	@Test
	public void t_200_POST_addDoublePostalAddress_ShouldWork() throws Exception
	{		
		long employeeId = this.createFreshEmployeeAndReturnOid();
		this.addSinglePostalAddress(employeeId, 1);
		this.addSinglePostalAddress(employeeId, 2);
		
		String response = given()
	        .spec(requestSpecBuilder.build())
	        .get(PATH_TO_EMPLOYEE_RESOURCE + "/" + employeeId + PATH_TO_ADDRESS_RESOURCE).asString();
		int count = from(response).getList("").size();
		assertThat(count, equalTo(2));
	}

	@Test
	public void t_200_PUT_updateSinglePostalAddress_ShouldWork() throws Exception
	{		
		long employeeId = this.createFreshEmployeeAndReturnOid();
		String path = this.addSinglePostalAddress(employeeId, 1);
		
		System.err.println("### " + path);
		
		Response oldResponse = given()
	        .spec(requestSpecBuilder.build())
	        .get(path);		
		int oid = oldResponse.path(EmployeePostalAddress_.DTO_NAME_oid);
		String oldHouseNumber = oldResponse.path(EmployeePostalAddress_.DTO_NAME_houseNumber);
		String newCity = "London";
		String newStreet = "Broadway";
				
		String jsonPayload = Json.createObjectBuilder()
		    .add(EmployeePostalAddress_.DTO_NAME_oid, oid)
		    .add(EmployeePostalAddress_.DTO_NAME_ranking, (int) oldResponse.path(EmployeePostalAddress_.DTO_NAME_ranking))
		    .add(EmployeePostalAddress_.DTO_NAME_countryCode, (String) oldResponse.path(EmployeePostalAddress_.DTO_NAME_countryCode))
		    .add(EmployeePostalAddress_.DTO_NAME_city, newCity)
		    //.add(EmployeePostalAddress_.DTO_NAME_secondaryAddressLine, (String) oldResponse.path(EmployeePostalAddress_.DTO_NAME_secondaryAddressLine))
		    .add(EmployeePostalAddress_.DTO_NAME_street, newStreet)
		    .add(EmployeePostalAddress_.DTO_NAME_houseNumber, (String) oldResponse.path(EmployeePostalAddress_.DTO_NAME_houseNumber))
		    //.add(EmployeePostalAddress_.DTO_NAME_poBoxNumber, (String) oldResponse.path(EmployeePostalAddress_.DTO_NAME_poBoxNumber))
		    .build()
		    .toString();
		
		ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
		noContentInResponse.expectBody(is("")).expectContentType("");

		given().spec(requestSpecBuilder.build())
			.body(jsonPayload)
			.when().put(path).then()
			.statusCode(HttpURLConnection.HTTP_NO_CONTENT).spec(noContentInResponse.build());

		// Now use the GET URI again and check the new values:
		given().spec(requestSpecBuilder.build()).when().get(path).then().statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON).body(Employee_.DTO_NAME_oid, is(oid))
			.body(EmployeePostalAddress_.DTO_NAME_city, is(newCity))
			.body(EmployeePostalAddress_.DTO_NAME_street, is(newStreet))
			.body(EmployeePostalAddress_.DTO_NAME_houseNumber, is(oldHouseNumber));
	}

	@Test
	public void t_400_DELETE_existing_shouldReturnStatusNoContent() throws Exception
	{
		long employeeId = this.createFreshEmployeeAndReturnOid();
		String path = this.addSinglePostalAddress(employeeId, 1);
		
	    ResponseSpecBuilder noContentInResponse = new ResponseSpecBuilder();
	    noContentInResponse.expectBody(is("")).expectContentType("");
	    
	    given()
	        .spec(requestSpecBuilder.build())
	        .log().headers()
	    .when()
	        .delete(path)
	    .then()
	        .statusCode(HttpURLConnection.HTTP_NO_CONTENT)
        	.spec(noContentInResponse.build());
	}
	
	// ------------------------------------------------------------------------------------------

	private String addSinglePostalAddress(long employeeId, int ranking) throws Exception
	{				
		String getUriEmployee = PATH_TO_EMPLOYEE_RESOURCE + "/" + employeeId;		
		String jsonPayload = Json.createObjectBuilder()
			.add(EmployeePostalAddress_.DTO_NAME_city, "city")
			.add(EmployeePostalAddress_.DTO_NAME_countryCode, "DE")
			.add(EmployeePostalAddress_.DTO_NAME_houseNumber, "1")
			.add(EmployeePostalAddress_.DTO_NAME_postalCode, "12345")
			.add(EmployeePostalAddress_.DTO_NAME_street, "street")
			.add(EmployeePostalAddress_.DTO_NAME_ranking, ranking)
			.build().toString();

		System.err.println("######## " + jsonPayload);

		Response response =
			given()
				.spec(requestSpecBuilder.build())
				.body(jsonPayload)
				.log().headers()
			.when()
				.post(getUriEmployee + PATH_TO_ADDRESS_RESOURCE)
			.then()
				.statusCode(HttpURLConnection.HTTP_CREATED)
				.headers("location", containsString(getUriEmployee))
				.and().extract().response();
		
		// Now get header location, use it and check the values:
		String getUriAddress = response.header("location");
		
		given()
			.spec(requestSpecBuilder.build())
		.when()
			.get(getUriAddress)
		.then()
			.log().body()
			.statusCode(HttpURLConnection.HTTP_OK)
			.contentType(ContentType.JSON)
			.body(EmployeePostalAddress_.DTO_NAME_city, is("city"))
			.body(EmployeePostalAddress_.DTO_NAME_street, is("street"));
		
		return getUriAddress;
	}
	
	private long createFreshEmployeeAndReturnOid()
	{
		TestPmsCoreApi_Employee employeeTest = new TestPmsCoreApi_Employee();
		try
		{
			TestPmsCoreApi_Employee.setupConnection();
			employeeTest.setup();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return employeeTest.createFreshEntityAndReturnOid();
	}
	
	private long getEmployeeOidByIdentification(String personnelNumber)
	{
		TestPmsCoreApi_Employee employeeTest = new TestPmsCoreApi_Employee();
		try
		{
			TestPmsCoreApi_Employee.setupConnection();
			employeeTest.setup();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return employeeTest.getOidByIdentification(personnelNumber);
	}
}