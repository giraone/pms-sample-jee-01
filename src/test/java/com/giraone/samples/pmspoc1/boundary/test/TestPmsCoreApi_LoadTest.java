package com.giraone.samples.pmspoc1.boundary.test;

import static com.jayway.restassured.RestAssured.given;

import java.io.StringReader;
import java.net.HttpURLConnection;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.giraone.samples.pmspoc1.boundary.test.loader.SimpleTestDataGenerator;
import com.giraone.samples.pmspoc1.entity.CostCenter_;
import com.giraone.samples.pmspoc1.entity.Employee_;
import com.giraone.samples.pmspoc1.entity.enums.EnumGender;
import com.jayway.restassured.response.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPmsCoreApi_LoadTest extends TestPmsCoreApi
{	
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

	@Test
	public void t_100_create100CostCenters() throws Exception
	{	
		for (int i = 0; i < 100; i++)
		{
			this.createCostCenter(i);
		}
	}
	
	@Test
	public void t_200_create1000Employees() throws Exception
	{
		JsonArray allCostCenters = this.loadAllCostCenters();
		for (int i = 1; i < 1000; i++)
		{
			this.createEmployee(i, allCostCenters);
		}
	}
	
	private void createCostCenter(int i) throws Exception
	{	
		String identification = String.format("K%05d", i);
		String description = SimpleTestDataGenerator.randomDepartment() + " " + identification;
		
		String jsonPayload = Json.createObjectBuilder()
		    .add(CostCenter_.DTO_NAME_identification, identification)
		    .add(CostCenter_.DTO_NAME_description, description)
		    .build()
		    .toString();
		
		given()
	        .spec(requestSpecBuilder.build())
	        .body(jsonPayload)
	    .when()
	        .post(TestPmsCoreApi_CostCenter.PATH_TO_RESOURCE)
	    .then()
	        .statusCode(HttpURLConnection.HTTP_CREATED);
	}
	
	private void createEmployee(int i, JsonArray allCostCenters) throws Exception
	{
		JsonObject costCenter = (JsonObject) allCostCenters.get(i % 100);
		String personnelNumber = String.format("%05d", i);
		EnumGender gender = SimpleTestDataGenerator.randomGender();
		
		String jsonPayload = Json.createObjectBuilder()
			.add(Employee_.DTO_NAME_costCenter, Json.createObjectBuilder()
				.add(CostCenter_.DTO_NAME_oid, costCenter.getInt(CostCenter_.DTO_NAME_oid)))
			.add(Employee_.DTO_NAME_personnelNumber, personnelNumber)
			.add(Employee_.DTO_NAME_lastName, SimpleTestDataGenerator.randomLastName())
			.add(Employee_.DTO_NAME_firstName, SimpleTestDataGenerator.randomFirstName(gender))
			.add(Employee_.DTO_NAME_dateOfBirth, SimpleTestDataGenerator.randomDateOfBirth().getTime().getTime())
			.add(Employee_.DTO_NAME_gender, gender.toString())
			.add(Employee_.DTO_NAME_dateOfEntry, SimpleTestDataGenerator.randomDateOfEntry().getTime().getTime())
			.add(Employee_.DTO_NAME_nationalityCode, SimpleTestDataGenerator.randomNationality())
			.build().toString();
		
		given()
	        .spec(requestSpecBuilder.build())
	        .body(jsonPayload)
	    .when()
	        .post(TestPmsCoreApi_Employee.PATH_TO_RESOURCE)
	    .then()
	        .statusCode(HttpURLConnection.HTTP_CREATED);
	}
	
	private JsonArray loadAllCostCenters() throws Exception
	{	
		Response response = given()
	        .spec(requestSpecBuilder.build())
			.get(TestPmsCoreApi_CostCenter.PATH_TO_RESOURCE);
				
		JsonReader reader = Json.createReader(new StringReader(response.asString()));      
        return reader.readArray();
	}
}