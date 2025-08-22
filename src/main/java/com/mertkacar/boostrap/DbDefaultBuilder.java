package com.mertkacar.boostrap;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DbDefaultBuilder {

    private static final Logger log = LoggerFactory.getLogger(DbDefaultBuilder.class);

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUser;

    @Value("${keycloak.admin.password}")
    private String adminPass;

    @PostConstruct
    public void init() {
        log.info("[KC Bootstrap] serverUrl={}, realm={}, clientId={}", serverUrl, realmName, clientId);

        Keycloak kc = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUser)
                .password(adminPass)
                .build();

        try {
            smokeTest(kc);

            ensureRealmExists(kc, realmName);
            RealmResource rr = kc.realm(realmName);

            String clientUuid = ensureClientExists(rr, clientId);
            log.info("[KC Bootstrap] clientUuid={}", clientUuid);

//            ensureClientRole(rr, clientUuid, "ADMIN");
//            ensureClientRole(rr, clientUuid, "USER");
//            ensureRealmRole(rr, "ADMIN");
//            ensureRealmRole(rr, "USER");
              ensureClientRole(rr, clientUuid, "ROLE_ADMIN");
              ensureClientRole(rr, clientUuid, "ROLE_USER");
              ensureRealmRole(rr, "ROLE_ADMIN");
              ensureRealmRole(rr, "ROLE_USER");

            String adminId = ensureUser(rr, "admin", "admin@enterprise.local", true);
            String userId  = ensureUser(rr, "user",  "user@enterprise.local",  true);

            setPassword(rr, adminId, "admin", false);
            setPassword(rr, userId,  "user",  false);

                assignClientRoles(rr, clientUuid, adminId, List.of("ROLE_ADMIN", "ROLE_USER"));
            assignClientRoles(rr, clientUuid, userId,  List.of("ROLE_USER"));


            log.info("[KC Bootstrap] OK ✓");
        }
        catch (ProcessingException e) {
            log.error("[KC Bootstrap] Keycloak'a bağlanılamadı. URL/port doğru mu? serverUrl={}. Keycloak ayakta mı?",
                    serverUrl, e);
            throw e;
        }
        catch (ClientErrorException e) {
            int status = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            log.error("[KC Bootstrap] HTTP {} hatası. Çoğunlukla yanlış admin cred veya 'admin-cli' direct access kapalı.",
                    status, e);
            throw e;
        }
        catch (Exception e) {
            log.error("[KC Bootstrap] Beklenmeyen hata.", e);
            throw e;
        }
    }

    private void smokeTest(Keycloak kc) {
        var realms = kc.realms().findAll();
        log.info("[KC Bootstrap] admin erişimi OK. Toplam realm: {}", realms.size());
    }

    private void ensureRealmExists(Keycloak kc, String realm) {
        try {
            kc.realm(realm).toRepresentation();
            log.info("[KC Bootstrap] realm zaten var: {}", realm);
        } catch (NotFoundException e) {
            log.warn("[KC Bootstrap] realm yok, oluşturuluyor: {}", realm);
            RealmRepresentation rep = new RealmRepresentation();
            rep.setRealm(realm);
            rep.setEnabled(true);
            kc.realms().create(rep);
            kc.realm(realm).toRepresentation();
            log.info("[KC Bootstrap] realm oluşturuldu: {}", realm);
        }
    }

    private String ensureClientExists(RealmResource rr, String clientId) {
        ClientsResource clients = rr.clients();
        List<ClientRepresentation> found = clients.findByClientId(clientId);
        if (found != null && !found.isEmpty()) {
            log.info("[KC Bootstrap] client zaten var: {}", clientId);
            return found.get(0).getId();
        }
        log.warn("[KC Bootstrap] client yok, oluşturuluyor: {}", clientId);
        ClientRepresentation cr = new ClientRepresentation();
        cr.setClientId(clientId);
        cr.setName(clientId);
        cr.setPublicClient(true);
        cr.setDirectAccessGrantsEnabled(true);
        cr.setServiceAccountsEnabled(false);

        Response resp = rr.clients().create(cr);
        if (resp.getStatus() >= 300) {
            throw new IllegalStateException("Client create failed: HTTP " + resp.getStatus());
        }
        String id = CreatedResponseUtil.getCreatedId(resp);
        log.info("[KC Bootstrap] client oluşturuldu: {} ({})", clientId, id);
        return id;
    }

    private void ensureRealmRole(RealmResource rr, String roleName) {
        try {
            rr.roles().get(roleName).toRepresentation();
        } catch (NotFoundException e) {
            RoleRepresentation rep = new RoleRepresentation();
            rep.setName(roleName);
            rr.roles().create(rep);
            log.info("[KC Bootstrap] realm role oluşturuldu: {}", roleName);
        }
    }

    private void ensureClientRole(RealmResource rr, String clientUuid, String roleName) {
        try {
            rr.clients().get(clientUuid).roles().get(roleName).toRepresentation();
        } catch (NotFoundException e) {
            RoleRepresentation rep = new RoleRepresentation();
            rep.setName(roleName);
            rr.clients().get(clientUuid).roles().create(rep);
            log.info("[KC Bootstrap] client role oluşturuldu: {}", roleName);
        }
    }

    private String ensureUser(RealmResource rr, String username, String email, boolean emailVerified) {
        var list = rr.users().search(username, true);
        var found = list.stream().filter(u -> username.equalsIgnoreCase(u.getUsername())).findFirst();

        if (found.isPresent()) {
            String userId = found.get().getId();
            UserRepresentation ur = rr.users().get(userId).toRepresentation();
            ur.setEmail(email);
            ur.setEnabled(true);
            ur.setEmailVerified(emailVerified);

            if (ur.getFirstName() == null || ur.getFirstName().isBlank())
                ur.setFirstName(cap(username));
            if (ur.getLastName() == null || ur.getLastName().isBlank())
                ur.setLastName("User");

            ur.setRequiredActions(new ArrayList<>());

            rr.users().get(userId).update(ur);
            log.info("[KC Bootstrap] kullanıcı güncellendi: {} ({})", username, userId);
            return userId;
        }

        UserRepresentation ur = new UserRepresentation();
        ur.setUsername(username);
        ur.setEmail(email);
        ur.setEnabled(true);
        ur.setEmailVerified(emailVerified);
        ur.setFirstName(cap(username));
        ur.setLastName("User");
        ur.setRequiredActions(new ArrayList<>());

        Response resp = rr.users().create(ur);
        if (resp.getStatus() >= 300) {
            throw new IllegalStateException("User create failed: HTTP " + resp.getStatus());
        }
        String id = CreatedResponseUtil.getCreatedId(resp);
        log.info("[KC Bootstrap] kullanıcı oluşturuldu: {} ({})", username, id);
        return id;
    }

    private String cap(String s) {
        if (s == null || s.isBlank()) return "User";
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void setPassword(RealmResource rr, String userId, String rawPassword, boolean temporary) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(temporary);
        cred.setValue(rawPassword);
        rr.users().get(userId).resetPassword(cred);
        log.info("[KC Bootstrap] parola set edildi (temporary={}): {}", temporary, userId);
    }

    private void assignClientRoles(RealmResource rr, String clientUuid, String userId, List<String> roleNames) {
        var clientRolesRes = rr.clients().get(clientUuid).roles();
        var reps = new ArrayList<RoleRepresentation>();
        for (String rn : roleNames) {
            reps.add(clientRolesRes.get(rn).toRepresentation());
        }
        rr.users().get(userId).roles().clientLevel(clientUuid).add(reps);
        log.info("[KC Bootstrap] client roles atandı: {} -> {}", userId, roleNames);
    }
}
