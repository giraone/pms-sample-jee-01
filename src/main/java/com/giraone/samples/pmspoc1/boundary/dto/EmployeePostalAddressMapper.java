package com.giraone.samples.pmspoc1.boundary.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress;

@Mapper
public interface EmployeePostalAddressMapper
{
	EmployeePostalAddressMapper INSTANCE = Mappers.getMapper(EmployeePostalAddressMapper.class);
	
	@Mapping(source = "employee.oid", target = "employeeId")
    void updateDtoFromEntity(EmployeePostalAddress entity, @MappingTarget EmployeePostalAddressDTO dto);
	@Mapping(source = "employee.oid", target = "employeeId")
	EmployeePostalAddressDTO entityToDto(EmployeePostalAddress entity);
	
	@Mapping(target="employee", ignore=true) 
    void updateEntityFromDto(EmployeePostalAddressDTO dto, @MappingTarget EmployeePostalAddress entity);
	@Mapping(target="employee", ignore=true) 
    EmployeePostalAddress dtoToEntity(EmployeePostalAddressDTO entity);
}