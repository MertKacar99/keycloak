package com.mertkacar.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// KeycloakAdminConfig.java
@Configuration
public class KeycloakAdminConfig {

    @Value("${keycloak.server-url}") String serverUrl;
    @Value("${keycloak.realm}") String realm;
    @Value("${keycloak.admin.username}") String adminUser;
    @Value("${keycloak.admin.password}") String adminPass;
//    @Value("${keycloak.client-id}") String clientId;

    @Bean
    public org.keycloak.admin.client.Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUser)
                .password(adminPass)
                .build();
    }

    @Bean
    public RealmResource realmResource(org.keycloak.admin.client.Keycloak kc) {
        return kc.realm(realm);
    }
}
