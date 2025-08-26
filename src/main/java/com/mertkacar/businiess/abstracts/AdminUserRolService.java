package com.mertkacar.businiess.abstracts;

import com.mertkacar.dto.requests.AssignRolesRequest;

import java.util.Map;

public interface AdminUserRolService {
    Map<String, Object> assignRoles(AssignRolesRequest req);
}