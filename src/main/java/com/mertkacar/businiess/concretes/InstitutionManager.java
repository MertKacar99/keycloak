package com.mertkacar.businiess.concretes;


import com.mertkacar.businiess.abstracts.InstitutionService;
import com.mertkacar.model.Institution;
import com.mertkacar.repository.InstitutionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InstitutionManager implements InstitutionService {
    private final InstitutionRepository institutionRepository;

    @Transactional
    @Override
    public Institution create(Institution req) {
        try {
            Institution institution = Institution.builder().name(req.getName() != null ? req.getName() : "").build();

            return institutionRepository.save(institution);

        } catch (Exception e) {
            throw new RuntimeException(e);

        }


    }

    @Override
    public List<Institution> list() {
         List<Institution> institution = institutionRepository.findAll();
         return institution;
    }

    @Override
    public Institution get(UUID id) {
     Institution institution =    institutionRepository.findById(id).orElse(null);
     return institution;
    }
}