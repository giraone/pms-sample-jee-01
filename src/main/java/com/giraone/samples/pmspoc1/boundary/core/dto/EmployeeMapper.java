package com.giraone.samples.pmspoc1.boundary.core.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.giraone.samples.pmspoc1.entity.CostCenter;
import com.giraone.samples.pmspoc1.entity.Employee;
import com.giraone.samples.pmspoc1.entity.EmployeePostalAddress;

@Mapper(componentModel = "cdi")
public interface EmployeeMapper
{
	EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);
	
    void updateDtoFromEntity(Employee entity, @MappingTarget EmployeeDTO dto);
    void updateEntityFromDto(EmployeeDTO dto, @MappingTarget Employee entity);
    
    void updateDtoFromEntity(CostCenter entity, @MappingTarget CostCenterDTO dto);
    void updateEntityFromDto(CostCenterDTO dto, @MappingTarget CostCenter entity);
    
    void updateDtoFromEntity(EmployeePostalAddress entity, @MappingTarget EmployeePostalAddressDTO dto);
    void updateEntityFromDto(EmployeePostalAddressDTO dto, @MappingTarget EmployeePostalAddress entity);
}