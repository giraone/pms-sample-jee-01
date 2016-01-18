package com.giraone.samples.pmspoc1.boundary.dto;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.giraone.samples.pmspoc1.entity.CostCenter;

@Mapper
public interface CostCenterMapper
{
	CostCenterMapper INSTANCE = Mappers.getMapper(CostCenterMapper.class);
	
    void updateDtoFromEntity(CostCenter entity, @MappingTarget CostCenterDTO dto);
    void updateEntityFromDto(CostCenterDTO dto, @MappingTarget CostCenter entity);
}