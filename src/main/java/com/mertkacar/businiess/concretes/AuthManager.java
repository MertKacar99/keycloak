package com.mertkacar.businiess.concretes;

import com.mertkacar.businiess.abstracts.AuthService;
import com.mertkacar.dto.requests.RegisterRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AuthManager implements AuthService {

    private final RealmResource realm;

    @Override
    public String register(RegisterRequest req) {
        // 1) UserRepresentation
        var user = new UserRepresentation();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Custom attributes
        Map<String, List<String>> attrs = new HashMap<>();
        if (req.getUserCode() != null) {
            attrs.put("userCode", List.of(req.getUserCode()));
        }
        user.setAttributes(attrs);

        // 2) Create
        Response response = realm.users().create(user);
        if (response.getStatus() >= 300) {
            throw new IllegalStateException("Keycloak user create failed: " + response.getStatusInfo());
        }
        String userId = CreatedResponseUtil.getCreatedId(response);

        // 3) Set password
        var cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(true);
        cred.setValue(req.getPassword());
        realm.users().get(userId).resetPassword(cred);

        // 4) Assign realm roles
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            var availableRoles = realm.roles().list();
            var toAssign = availableRoles.stream()
                    .filter(r -> req.getRoles().contains(r.getName()))
                    .collect(Collectors.toList());
            if (!toAssign.isEmpty()) {
                realm.users().get(userId).roles().realmLevel().add(toAssign);
            }
        }

        return userId;
    }
}
