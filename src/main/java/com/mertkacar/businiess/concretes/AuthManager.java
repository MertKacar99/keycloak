package com.mertkacar.businiess.concretes;

import com.mertkacar.businiess.abstracts.AuthService;
import com.mertkacar.dto.requests.RegisterRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthManager implements AuthService {

    private final RealmResource realm;

    @Value("${keycloak.client-id}")
    private String appClientId;

    @Override
    public String register(RegisterRequest req) {

        // 0) Basit doğrulamalar
        if (req.getUsername() == null || req.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username boş olamaz");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email boş olamaz");
        }
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password boş olamaz");
        }

        // 1) Kullanıcı zaten var mı? (username ve/veya email)
        var existingUsersByUsername = realm.users().search(req.getUsername(), true);
        boolean usernameExists = existingUsersByUsername.stream()
                .anyMatch(u -> req.getUsername().equalsIgnoreCase(u.getUsername()));
        if (usernameExists) {
            throw new IllegalStateException("Username zaten kullanılıyor: " + req.getUsername());
        }

        var existingUsersByEmail = realm.users().searchByEmail(req.getEmail(), true);
        boolean emailExists = existingUsersByEmail != null && !existingUsersByEmail.isEmpty();
        if (emailExists) {
            throw new IllegalStateException("Email zaten kullanılıyor: " + req.getEmail());
        }

        // 2) ROL ÖN-KONTROLÜ (Kullanıcı oluşturulmadan önce)
        // 2.a) Realm rolleri
        List<String> requestedRealmRoles = (req.getRoles() == null) ? List.of() : req.getRoles();
        var realmRoleNames = realm.roles().list().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());
        var missingRealmRoles = requestedRealmRoles.stream()
                .filter(rn -> !realmRoleNames.contains(rn))
                .collect(Collectors.toList());
        if (!missingRealmRoles.isEmpty()) {
            throw new IllegalStateException("Eksik realm role(ler): " + String.join(", ", missingRealmRoles));
        }

        // 2.b) Client rolleri
        List<String> requestedClientRoles = (req.getClientRoles() == null) ? List.of() : req.getClientRoles();
        String clientUuid = null;
        Map<String, org.keycloak.representations.idm.RoleRepresentation> clientRoleMap = new HashMap<>();
        if (!requestedClientRoles.isEmpty()) {
            var clients = realm.clients().findByClientId(appClientId);
            if (clients == null || clients.isEmpty()) {
                throw new IllegalStateException("Client bulunamadı: " + appClientId);
            }
            clientUuid = clients.get(0).getId();

            var existingClientRoles = realm.clients().get(clientUuid).roles().list();
            var existingClientRoleNames = existingClientRoles.stream()
                    .peek(cr -> clientRoleMap.put(cr.getName(), cr))
                    .map(cr -> cr.getName())
                    .collect(Collectors.toSet());

            var missingClientRoles = requestedClientRoles.stream()
                    .filter(cr -> !existingClientRoleNames.contains(cr))
                    .collect(Collectors.toList());

            if (!missingClientRoles.isEmpty()) {
                throw new IllegalStateException("Eksik client role(ler): " + String.join(", ", missingClientRoles)
                        + " (client=" + appClientId + ")");
            }
        }

        // 3) UserRepresentation
        var user = new UserRepresentation();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

        // Custom attributes (TODO: DB'den attribute getirilecekse burada zenginleştir)
        Map<String, List<String>> attrs = new HashMap<>();
        if (req.getUserCode() != null && !req.getUserCode().isBlank()) {
            attrs.put("userCode", List.of(req.getUserCode()));
        }
        user.setAttributes(attrs);

        String userId = null;
        try {
            // 4) Kullanıcı oluştur
            Response response = realm.users().create(user);
            if (response.getStatus() >= 300) {
                throw new IllegalStateException("Keycloak user create failed: " + response.getStatusInfo());
            }
            userId = CreatedResponseUtil.getCreatedId(response);

            // 5) Şifre ata
            var cred = new CredentialRepresentation();
            cred.setType(CredentialRepresentation.PASSWORD);
            cred.setTemporary(false);
            cred.setValue(req.getPassword());
            realm.users().get(userId).resetPassword(cred);

            // 6) Realm role ata (liste boş değilse)
            if (!requestedRealmRoles.isEmpty()) {
                var toAssign = realm.roles().list().stream()
                        .filter(r -> requestedRealmRoles.contains(r.getName()))
                        .collect(Collectors.toList());
                if (!toAssign.isEmpty()) {
                    realm.users().get(userId).roles().realmLevel().add(toAssign);
                }
            }

            // 7) Client role ata (liste boş değilse)
            if (!requestedClientRoles.isEmpty()) {
                var rolesToAssign = requestedClientRoles.stream()
                        .map(clientRoleMap::get)
                        .collect(Collectors.toList());
                if (!rolesToAssign.isEmpty()) {
                    realm.users().get(userId)
                            .roles()
                            .clientLevel(clientUuid)
                            .add(rolesToAssign);
                }
            }

            // 8) OK
            return userId;
        } catch (RuntimeException ex) {
            // Bir yerde hata olursa oluşturduğumuz kullanıcıyı geri temizlemeye çalışalım
            if (userId != null) {
                try {
                    realm.users().get(userId).remove();
                } catch (Exception ignore) { }
            }
            throw ex;
        }
    }
}

