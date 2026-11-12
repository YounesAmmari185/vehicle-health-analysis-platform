package com.rdm.analysis.dto;

public record RecallResponse(
        boolean active,
        String campaignName,
        String description
) {}
