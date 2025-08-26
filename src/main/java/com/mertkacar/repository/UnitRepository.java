package com.mertkacar.repository;

import com.mertkacar.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitRepository  extends JpaRepository<Unit, UUID> {
    Optional<Unit> findByName(String name);
//    List<Unit> findAllByInstitution_Id(String institutionId);
}
