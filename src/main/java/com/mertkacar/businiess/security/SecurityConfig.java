package com.mertkacar.businiess.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.util.*;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(rc -> rc.requestCache(new NullRequestCache()))
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(auth -> auth
                        // === WHITELIST ===
                        .requestMatchers("/api/public/**").permitAll()
                        // === Geri kalan ===
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter(null)))
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain permitAllElse(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .requestCache(rc -> rc.requestCache(new NullRequestCache()))
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/configuration/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${keycloak.client-id}") String clientId) {

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<org.springframework.security.core.GrantedAuthority> out = new ArrayList<>();

            // === Realm roles -> ROLE_* ===
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                Object rolesObj = realmAccess.get("roles");
                if (rolesObj instanceof Collection<?> roles) {
                    roles.forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + r.toString())));
                }
            }

            // === Client roles -> PERM_* ===
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> app = (Map<String, Object>) resourceAccess.get(clientId);
                if (app != null) {
                    Object rolesObj = app.get("roles");
                    if (rolesObj instanceof Collection<?> roles) {
                        roles.forEach(r -> out.add(new SimpleGrantedAuthority("PERM_" + r.toString())));
                    }
                }
            }

            return out; // ✅ Artık List<GrantedAuthority> döndürüyoruz
        });
        return converter;
    }

}
