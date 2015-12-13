package com.giraone.samples.pmspoc1.boundary.test;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.net.HttpURLConnection;

import javax.json.Json;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giraone.samples.common.boundary.model.ErrorInformation;
import com.giraone.samples.pmspoc1.entity.CostCenter_;
import com.giraone.samples.pmspoc1.entity.Employee_;
import com.giraone.samples.pmspoc1.entity.enums.EnumGender;

/*
 * These tests are for checking the various validation scenarios for the Employee REST end point.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestPmsCoreApi_EmployeeValidation extends TestPmsCoreApi
{
	static final String PATH_TO_RESOURCE = "/employees";
	static final String ENTITY_VALID_domainKey = "123456";
	
	// For re-use of test tools
	TestPmsCoreApi_Employee testPmsCoreApi_Employee;
	
	// A Jackson2 object mapper
	ObjectMapper mapper = new ObjectMapper();
	
	@BeforeClass
	public static void setupConnection()
	{
		TestPmsCoreApi.setupConnection();
	}

	@Before
	public void setup() throws Exception
	{
		super.setup();
		testPmsCoreApi_Employee = new TestPmsCoreApi_Employee();
		testPmsCoreApi_Employee.setup();
	}

	// ------------------------------------------------------------------------------------------

	@Test
	public void t_100_POST_wrongGender_shouldReturnBadRequest() throws Exception
	{
		this.testPmsCoreApi_Employee.deleteEntityByIdentificationAndIgnoreStatus(ENTITY_VALID_domainKey);
		
		long costCenterId = this.testPmsCoreApi_Employee.createFreshCostCenterAndReturnOid("CO1234");
		
		String jsonPayload = Json.createObjectBuilder()
			.add(Employee_.DTO_NAME_costCenter, Json.createObjectBuilder()
				.add(CostCenter_.DTO_NAME_oid, costCenterId))
			.add(Employee_.DTO_NAME_personnelNumber, ENTITY_VALID_domainKey)
			.add(Employee_.DTO_NAME_lastName, "Doe")
			.add(Employee_.DTO_NAME_firstName, "Jane")
			.add(Employee_.DTO_NAME_dateOfBirth, "1966-12-31T00:00:00.000Z")
			.add(Employee_.DTO_NAME_gender, "XXX")
			.build().toString();

		given()
			.spec(requestSpecBuilder.build())
			.body(jsonPayload)
			.log().all()
		.when()
			.post(PATH_TO_RESOURCE)
		.then()
			.log().all()
			.statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
			.body("code", is(ErrorInformation.FIELD_VALIDATION_FAILED));		
	}
	
	@Test
	public void t_101_POST_wrongNationalityCode_shouldReturnBadRequest() throws Exception
	{
		this.testPmsCoreApi_Employee.deleteEntityByIdentificationAndIgnoreStatus(ENTITY_VALID_domainKey);
		
		long costCenterId = this.testPmsCoreApi_Employee.createFreshCostCenterAndReturnOid("CO1234");
		
		String jsonPayload = Json.createObjectBuilder()
			.add(Employee_.DTO_NAME_costCenter, Json.createObjectBuilder()
				.add(CostCenter_.DTO_NAME_oid, costCenterId))
			.add(Employee_.DTO_NAME_personnelNumber, ENTITY_VALID_domainKey)
			.add(Employee_.DTO_NAME_lastName, "Doe")
			.add(Employee_.DTO_NAME_firstName, "Jane")
			.add(Employee_.DTO_NAME_dateOfBirth, "1966-12-31T00:00:00.000Z")
			.add(Employee_.DTO_NAME_gender, EnumGender.F.toString())
			.add(Employee_.DTO_NAME_nationalityCode, "XXX")
			.add(Employee_.DTO_NAME_countryOfBirth, "DE")
			.build().toString();

		given()
			.spec(requestSpecBuilder.build())
			.body(jsonPayload)
			.log().all()
		.when()
			.post(PATH_TO_RESOURCE)
		.then()
			.log().all()
			.statusCode(HttpURLConnection.HTTP_BAD_REQUEST)
			.body("code", is(ErrorInformation.FIELD_VALIDATION_FAILED));		
	}
}