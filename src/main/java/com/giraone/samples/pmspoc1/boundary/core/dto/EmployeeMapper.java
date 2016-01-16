package com.giraone.samples.pmspoc1.boundary.core.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.Employee_;

@Mapper(uses = { CostCenterMapper.class, EmployeePostalAddressMapper.class })
public interface EmployeeMapper
{
	EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

	void updateDtoFromEntity(Employee entity, @MappingTarget EmployeeDTO dto);

	
	@Mappings({
		@Mapping(target = "postalAddresses", ignore = true), // Is performed manually		
		@Mapping(target = "costCenter", ignore = true),      // Is performed manually
		@Mapping(target = "properties", ignore = true),      // Ignore warning from properties member
		
		// These mappings are only to ignore warnings from setter/getter detection of MapStruct
		@Mapping(target = EmployeeDTO_.DTO_NAME_nationalityCode, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_dateOfEntry, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_religion, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_maritalStatus, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_numberOfChildren, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_countryOfBirth, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_birthPlace, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_birthName, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_contactEmailAddress1, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_contactEmailAddress2, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_contactPhone1, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_contactPhone2, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_contactFax1, ignore = true),
		@Mapping(target = EmployeeDTO_.DTO_NAME_contactFax2, ignore = true) })
	void updateEntityFromDto(EmployeeDTO dto, @MappingTarget Employee entity);
	

	@Mappings({ @Mapping(target = EmployeeDTO_.DTO_NAME_postalAddresses, ignore = true), // Is performed manually
	})
	void updateDtoFromEntity(Employee entity, @MappingTarget EmployeeWithPropertiesDTO dto);

	
	@Mappings({
		@Mapping(target = EmployeeDTO_.DTO_NAME_postalAddresses, ignore = true), // Is performed manually		
		@Mapping(target = EmployeeDTO_.DTO_NAME_costCenter, ignore = true),      // Is performed manually
		@Mapping(target = Employee_.SQL_NAME_PROPERTIES, ignore = true),      // Ignore warning from properties member
	})
	void updateEntityFromDto(EmployeeWithPropertiesDTO dto, @MappingTarget Employee entity);
}