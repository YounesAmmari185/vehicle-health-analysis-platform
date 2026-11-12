package com.rdm.analysis.mapper;

import com.rdm.analysis.dto.AlertResponse;
import com.rdm.analysis.dto.HealthReportResponse;
import com.rdm.analysis.entity.AlertEntity;
import com.rdm.analysis.entity.HealthReportEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    HealthReportResponse toHealthReportResponse(HealthReportEntity entity);

    AlertResponse toAlertResponse(AlertEntity entity);
}
