package com.giraone.samples.pmspoc1.boundary.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.net.HttpURLConnection;

import javax.json.Json;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress_;
import com.giraone.samples.pmspoc1.entity.Employee_;
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
	public void t_200_POST_addPostalAddress() throws Exception
	{		
		long employeeId = this.createFreshEmployeeAndReturnOid();
		
		String getUriEmployee = PATH_TO_EMPLOYEE_RESOURCE + "/" + employeeId;
		//Response employeeResponse = given().spec(requestSpecBuilder.build()).get(getUriEmployee);
		
		String jsonPayload = Json.createObjectBuilder()
			.add(EmployeePostalAddress_.DTO_NAME_employeeId, Json.createObjectBuilder()
				.add(Employee_.DTO_NAME_oid, employeeId))
			.add(EmployeePostalAddress_.DTO_NAME_city, "city")
			.add(EmployeePostalAddress_.DTO_NAME_countryCode, "DE")
			.add(EmployeePostalAddress_.DTO_NAME_houseNumber, "1")
			.add(EmployeePostalAddress_.DTO_NAME_postalCode, "12345")
			.add(EmployeePostalAddress_.DTO_NAME_street, "street")
			.add(EmployeePostalAddress_.DTO_NAME_ranking, "1")
			.build().toString();

		System.err.println("######## " + jsonPayload);

		Response response =
			given()
				.spec(requestSpecBuilder.build())
				.body(jsonPayload)
				.log().all()
			.when()
				.post(getUriEmployee + PATH_TO_ADDRESS_RESOURCE)
			.then()
				.statusCode(HttpURLConnection.HTTP_CREATED)
				.headers("location", containsString(getUriEmployee + PATH_TO_ADDRESS_RESOURCE + "/"))
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
			.body(EmployeePostalAddress_.DTO_NAME_city, is("city"))
			.body(EmployeePostalAddress_.DTO_NAME_street, is("street"));
	}

	// ------------------------------------------------------------------------------------------
	
	private int createFreshEmployeeAndReturnOid()
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
}