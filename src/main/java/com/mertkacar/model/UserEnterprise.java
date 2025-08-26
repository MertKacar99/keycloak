package com.mertkacar.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_enterprise", indexes = {
        @Index(name = "ix_user_ent_kc_user_id", columnList = "keycloak_user_id", unique = true),
        @Index(name = "ix_user_ent_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEnterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Keycloak userId (token 'sub')
    @Column(name = "keycloak_user_id", nullable = false, unique = true, length = 64)
    private String keycloakUserId;

    @Column(length = 200, nullable = false)
    private String email;

    @Column(name = "first_name", length = 120)
    private String firstName;

    @Column(name = "last_name", length = 120)
    private String lastName;

    // İstersen token’daki custom claim
    @Column(name = "user_code", length = 120)
    private String userCode;

    // Kullanıcının kurumu (zorunlu yapabilirsin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_user_ent_institution"))
    private Institution institution;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserUnit> userUnits = new HashSet<>();

    // Basit ManyToMany istersen (join entity yerine):
    // @ManyToMany
    // @JoinTable(name = "user_unit",
    //     joinColumns = @JoinColumn(name = "user_id"),
    //     inverseJoinColumns = @JoinColumn(name = "unit_id"))
    // private Set<Unit> units = new HashSet<>();
}
