    package com.mertkacar.model;

    import jakarta.persistence.*;
    import lombok.*;

    import java.util.UUID;

    @Entity
    @Table(name = "institution", uniqueConstraints = {
            @UniqueConstraint(name = "uk_institution_name", columnNames = "name")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Institution {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(nullable = false, length = 200)
        private String name;
    }
