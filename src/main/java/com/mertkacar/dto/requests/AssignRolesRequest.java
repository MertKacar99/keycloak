package com.mertkacar.dto.requests;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRolesRequest {
    private String targetUserId;          // Rol atanacak kullanıcının Keycloak userId'si
    private List<String> realmRoles;      // Örn: ["ADMIN","USER"]
    private List<String> clientRoles;     // Örn: ["USERS_READ","USERS_UPDATE"]
}
