package com.rdm.telemetry.mapper;

import com.rdm.telemetry.dto.TelemetryRequest;
import com.rdm.telemetry.dto.TelemetryResponse;
import com.rdm.telemetry.entity.TelemetryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TelemetryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(
            target = "errorCodes",
            defaultExpression = "java(java.util.List.of())"
    )
    TelemetryEntity toEntity(TelemetryRequest request);

    @Mapping(
            target = "message",
            constant = "Telemetry received and saved successfully"
    )
    TelemetryResponse toResponse(TelemetryEntity entity);
}
