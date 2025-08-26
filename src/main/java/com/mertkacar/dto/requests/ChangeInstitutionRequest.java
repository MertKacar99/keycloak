package com.mertkacar.dto.requests;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeInstitutionRequest {
    private UUID userId;
    private UUID institutionId;
}