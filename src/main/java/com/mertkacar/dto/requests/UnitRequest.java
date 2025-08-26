package com.mertkacar.dto.requests;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitRequest {
    private String name;
    private UUID institutionId;
}
