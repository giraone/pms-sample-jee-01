package com.giraone.samples.pmspoc1.boundary.core.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress;

@Mapper(componentModel = "cdi")
public interface EmployeePostalAddressMapper
{
	EmployeePostalAddressMapper INSTANCE = Mappers.getMapper(EmployeePostalAddressMapper.class);
	
    void updateDtoFromEntity(EmployeePostalAddress entity, @MappingTarget EmployeePostalAddressDTO dto);
    void updateEntityFromDto(EmployeePostalAddressDTO dto, @MappingTarget EmployeePostalAddress entity);
}