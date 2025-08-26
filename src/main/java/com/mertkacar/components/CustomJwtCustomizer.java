package com.mertkacar.components;

import com.mertkacar.model.UserEnterprise;
import com.mertkacar.repository.UserEnterpriseRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CustomJwtCustomizer implements Converter<Jwt, AbstractAuthenticationToken> {
    private final UserEnterpriseRepository userRepo;

    public CustomJwtCustomizer(UserEnterpriseRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String sub = jwt.getSubject();

        // DB’de user var mı kontrol et
        UserEnterprise user = userRepo.findByKeycloakUserId(sub)
                .orElseThrow(() -> new IllegalStateException("UserEnterprise bulunamadı: " + sub));

        // Mevcut rolleri al
        Collection<SimpleGrantedAuthority> authorities =
                jwt.hasClaim("realm_access")
                        ? jwt.getClaimAsStringList("realm_access").stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .toList()
                        : List.of();

        // Ek claim: kurum ve birimler
        Map<String, Object> enrichedClaims = new HashMap<>(jwt.getClaims());
        if (user.getInstitution() != null) {
            enrichedClaims.put("institution", user.getInstitution().getName());
        }
        enrichedClaims.put("units", user.getUserUnits().stream()
                .map(uu -> uu.getUnit().getName())
                .toList());

        // Yeni JWT nesnesi
        Jwt enrichedJwt = Jwt.withTokenValue(jwt.getTokenValue())
                .headers(h -> h.putAll(jwt.getHeaders()))
                .claims(c -> c.putAll(enrichedClaims))
                .build();

        return new JwtAuthenticationToken(enrichedJwt, authorities, sub);
    }
}
