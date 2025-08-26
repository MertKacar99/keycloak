package com.mertkacar.dto.requests;

import com.mertkacar.model.Unit;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignUnitsRequest {
    private Unit userId;
    private List<UUID> unitIds;
}
