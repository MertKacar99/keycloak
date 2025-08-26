package com.mertkacar.businiess.abstracts;

import com.mertkacar.dto.requests.AssignUnitsRequest;
import com.mertkacar.dto.requests.ChangeInstitutionRequest;

import java.util.UUID;

public interface UserOrgService {
    void changeUserInstitution(ChangeInstitutionRequest req);
    void assignUnits(AssignUnitsRequest req);
    void removeUnit(UUID userId, UUID unitId);
}
