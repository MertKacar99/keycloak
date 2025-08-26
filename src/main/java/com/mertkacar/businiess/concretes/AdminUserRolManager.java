package com.mertkacar.businiess.concretes;

import com.mertkacar.businiess.abstracts.AdminUserRolService;
import com.mertkacar.dto.requests.AssignRolesRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserRolManager implements AdminUserRolService {
    private final RealmResource realm;

    @Value("${keycloak.client-id}")
    private String appClientId;


    // Sadece bu roller atanabilir (güvenlik için beyaz liste),
    // TODO:  Buradaki WHITE_LIST rolleri ve  permissionları Sistem parametrelerinden çekilebilir hale getirmelisin.
    private static final Set<String> ALLOWED_REALM_ROLES = Set.of("ROLE_ADMIN", "ROLE_USER");
    private static final Set<String> ALLOWED_CLIENT_ROLES = Set.of("USERS_READ", "USERS_UPDATE", "USERS_DELETE");

    @Transactional()
    @Override
    public Map<String, Object> assignRoles(AssignRolesRequest req) {
        if (req.getTargetUserId() == null || req.getTargetUserId().isBlank()) {
            throw new IllegalArgumentException("targetUserId zorunludur");
        }

        var userRes = realm.users().get(req.getTargetUserId());
        try {
            userRes.toRepresentation(); // yoksa 404’e düşer
        } catch (Exception e) {
            throw new IllegalArgumentException("Kullanıcı bulunamadı: " + req.getTargetUserId());
        }

        // İstenen roller
        var askedRealm = Optional.ofNullable(req.getRealmRoles()).orElse(List.of());
        var askedClient = Optional.ofNullable(req.getClientRoles()).orElse(List.of());

        // 1) Beyaz liste kontrolü
        var disallowedRealm = askedRealm.stream().filter(r -> !ALLOWED_REALM_ROLES.contains(r)).toList();
        if (!disallowedRealm.isEmpty()) {
            throw new IllegalArgumentException("İzin verilmeyen realm rol(ler): " + String.join(", ", disallowedRealm));
        }
        var disallowedClient = askedClient.stream().filter(r -> !ALLOWED_CLIENT_ROLES.contains(r)).toList();
        if (!disallowedClient.isEmpty()) {
            throw new IllegalArgumentException("İzin verilmeyen client rol(ler): " + String.join(", ", disallowedClient));
        }

        // 2) Mevcut rol varlığı kontrolü
        var existingRealm = realm.roles().list().stream()
                .collect(Collectors.toMap(RoleRepresentation::getName, r -> r));

        var missingRealm = askedRealm.stream().filter(r -> !existingRealm.containsKey(r)).toList();
        if (!missingRealm.isEmpty()) {
            throw new IllegalStateException("Eksik realm role(ler): " + String.join(", ", missingRealm));
        }

        String clientUuid = null;
        Map<String, RoleRepresentation> existingClient;

        if (!askedClient.isEmpty()) {
            var clients = realm.clients().findByClientId(appClientId);
            if (clients == null || clients.isEmpty()) {
                throw new IllegalStateException("Client bulunamadı: " + appClientId);
            }
            clientUuid = clients.get(0).getId();
            existingClient = realm.clients().get(clientUuid).roles().list().stream()
                    .collect(Collectors.toMap(RoleRepresentation::getName, r -> r));

            final var finalExistingClient = existingClient;
            var missingClient = askedClient.stream()
                    .filter(r -> !finalExistingClient.containsKey(r))
                    .toList();
            if (!missingClient.isEmpty()) {
                throw new IllegalStateException("Eksik client role(ler): " + String.join(", ", missingClient));
            }
        } else {
            existingClient = Map.of();
        }

        // 3) Atama
        if (!askedRealm.isEmpty()) {
            var toAdd = askedRealm.stream().map(existingRealm::get).toList();
            userRes.roles().realmLevel().add(toAdd);
        }
        if (!askedClient.isEmpty() && clientUuid != null) {
            var toAdd = askedClient.stream().map(existingClient::get).toList();
            userRes.roles().clientLevel(clientUuid).add(toAdd);
        }

        //  Response için Map dön
        return Map.of(
                "message", "Roller başarıyla atandı",
                "userId", req.getTargetUserId(),
                "realmRoles", askedRealm,
                "clientRoles", askedClient
        );
    }

}
