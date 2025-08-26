package com.mertkacar.businiess.abstracts;

import com.mertkacar.dto.requests.UnitRequest;
import com.mertkacar.model.Unit;

import java.util.List;
import java.util.UUID;

public interface UnitService {
    Unit create(UnitRequest req);
    List<Unit> list();
    Unit get(UUID id);
    String delete(UUID id);
}
