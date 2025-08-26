package com.mertkacar.businiess.abstracts;

import com.mertkacar.model.Institution;

import java.util.List;
import java.util.UUID;

public interface InstitutionService {
    Institution create(Institution req);
    List<Institution> list();
    Institution get(UUID id);
}
