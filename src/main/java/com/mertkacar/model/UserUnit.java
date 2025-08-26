    package com.mertkacar.model;

    import jakarta.persistence.*;
    import lombok.*;

    import java.util.UUID;

    @Entity
    @Table(name = "user_unit")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class UserUnit {
        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private UserEnterprise user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "unit_id")
        private Unit unit;
    }
