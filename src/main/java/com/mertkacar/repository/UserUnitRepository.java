package com.mertkacar.repository;

import com.mertkacar.model.UserUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserUnitRepository extends JpaRepository<UserUnit, UUID> {
//    boolean existsByUser_IdAndUnit_Id(String userId, String unitId);
//    List<UserUnit> findAllByUser_Id(String userId);
}
