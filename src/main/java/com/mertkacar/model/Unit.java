    package com.mertkacar.model;

    import jakarta.persistence.*;
    import lombok.*;

    import java.util.HashSet;
    import java.util.Set;
    import java.util.UUID;

    @Entity
    @Table(name = "unit", uniqueConstraints = {
            @UniqueConstraint(name = "uk_unit_name", columnNames = "name")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Unit {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(nullable = false, length = 200)
        private String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_unit_institution"))
        private Institution institution;

        @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<UserUnit> userUnits = new HashSet<>();

    }
