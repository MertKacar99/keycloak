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
import org.keycloak.representations.idm.*;
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

            // ==== 1) Permission (Client Role) tanımları ====
            ensureClientRole(rr, clientUuid, "USERS_READ");
            ensureClientRole(rr, clientUuid, "USERS_UPDATE");
            ensureClientRole(rr, clientUuid, "USERS_DELETE");

            // ==== 2) Realm Role (USER) ====
            ensureRealmRole(rr, "ROLEUSER");

            // ==== 3) Örnek kullanıcılar ====
            String viewerId    = ensureUser(rr, "viewer",    "viewer@enterprise.local",    true);
            String editorId    = ensureUser(rr, "editor",    "editor@enterprise.local",    true);
            String moderatorId = ensureUser(rr, "moderator", "moderator@enterprise.local", true);

            setPassword(rr, viewerId,    "viewer",    false);
            setPassword(rr, editorId,    "editor",    false);
            setPassword(rr, moderatorId, "moderator", false);

            // ==== 4) Client role assignment (permissions) ====
            assignClientRoles(rr, clientUuid, viewerId,    List.of("USERS_READ"));
            assignClientRoles(rr, clientUuid, editorId,    List.of("USERS_READ", "USERS_UPDATE"));
            assignClientRoles(rr, clientUuid, moderatorId, List.of("USERS_READ", "USERS_UPDATE", "USERS_DELETE"));

            // ==== 5) Realm role assignment ====
            assignRealmRoles(rr, viewerId,    List.of("USER"));
            assignRealmRoles(rr, editorId,    List.of("USER"));
            assignRealmRoles(rr, moderatorId, List.of("USER"));

            // ==== 6) Grup işlemleri ====
            String userGroupId = ensureGroup(rr, "user");
            addUserToGroup(rr, viewerId, userGroupId);
            addUserToGroup(rr, editorId, userGroupId);
            addUserToGroup(rr, moderatorId, userGroupId);

            log.info("[KC Bootstrap] OK ✓");
        }
        catch (ProcessingException e) {
            log.error("[KC Bootstrap] Keycloak'a bağlanılamadı. URL/port doğru mu? serverUrl={}", serverUrl, e);
            throw e;
        }
        catch (ClientErrorException e) {
            int status = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            log.error("[KC Bootstrap] HTTP {} hatası. Yanlış admin cred olabilir.", status, e);
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
            RealmRepresentation rep = new RealmRepresentation();
            rep.setRealm(realm);
            rep.setEnabled(true);
            kc.realms().create(rep);
            log.info("[KC Bootstrap] realm oluşturuldu: {}", realm);
        }
    }

    private String ensureClientExists(RealmResource rr, String clientId) {
        ClientsResource clients = rr.clients();
        List<ClientRepresentation> found = clients.findByClientId(clientId);
        if (found != null && !found.isEmpty()) {
            return found.get(0).getId();
        }
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
        return CreatedResponseUtil.getCreatedId(resp);
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
            return found.get().getId();
        }

        UserRepresentation ur = new UserRepresentation();
        ur.setUsername(username);
        ur.setEmail(email);
        ur.setEnabled(true);
        ur.setEmailVerified(emailVerified);
        ur.setFirstName(cap(username));
        ur.setLastName("User");

        Response resp = rr.users().create(ur);
        if (resp.getStatus() >= 300) {
            throw new IllegalStateException("User create failed: HTTP " + resp.getStatus());
        }
        return CreatedResponseUtil.getCreatedId(resp);
    }

    private String cap(String s) {
        return (s == null || s.isBlank()) ? "User"
                : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void setPassword(RealmResource rr, String userId, String rawPassword, boolean temporary) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(temporary);
        cred.setValue(rawPassword);
        rr.users().get(userId).resetPassword(cred);
    }

    private void assignClientRoles(RealmResource rr, String clientUuid, String userId, List<String> roleNames) {
        var clientRolesRes = rr.clients().get(clientUuid).roles();
        var reps = new ArrayList<RoleRepresentation>();
        for (String rn : roleNames) {
            reps.add(clientRolesRes.get(rn).toRepresentation());
        }
        rr.users().get(userId).roles().clientLevel(clientUuid).add(reps);
    }

    private void assignRealmRoles(RealmResource rr, String userId, List<String> roleNames) {
        var reps = new ArrayList<RoleRepresentation>();
        for (String rn : roleNames) {
            reps.add(rr.roles().get(rn).toRepresentation());
        }
        rr.users().get(userId).roles().realmLevel().add(reps);
    }

    private String ensureGroup(RealmResource rr, String groupName) {
        var groups = rr.groups().groups(groupName, 0, 1);
        if (groups != null && !groups.isEmpty()) {
            return groups.get(0).getId();
        }
        GroupRepresentation gr = new GroupRepresentation();
        gr.setName(groupName);
        Response resp = rr.groups().add(gr);
        if (resp.getStatus() >= 300) {
            throw new IllegalStateException("Group create failed: HTTP " + resp.getStatus());
        }
        return CreatedResponseUtil.getCreatedId(resp);
    }

    private void addUserToGroup(RealmResource rr, String userId, String groupId) {
        rr.users().get(userId).joinGroup(groupId);
    }
}
