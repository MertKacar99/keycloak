package com.mertkacar.businiess.concretes;

import com.mertkacar.businiess.abstracts.UnitService;
import com.mertkacar.dto.requests.UnitRequest;
import com.mertkacar.model.Unit;
import com.mertkacar.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UnitManager  implements UnitService {
    private final UnitRepository uniRepository;
    @Override
    public Unit create(UnitRequest req) {
         Unit unit = new Unit().builder()
                 .name(req.getName()).build();
         uniRepository.save(unit);
        return  unit;
    }

    @Override
    public List<Unit> list() {
      List<Unit> uni = uniRepository.findAll();
      return uni;
    }

    @Override
    public Unit get(UUID id) {
        Unit uni = uniRepository.findById(id).orElse(null);
        return uni;
    }

    @Override
    public String delete(UUID id) {
          Unit uni = uniRepository.findById(id).orElse(null);
          if(uni != null){
              uniRepository.deleteById(id);
          }else{
              return "Unit not found";
          }
             return   "Deleted unit:" + id + "li " + uni.getName();
    }
}
