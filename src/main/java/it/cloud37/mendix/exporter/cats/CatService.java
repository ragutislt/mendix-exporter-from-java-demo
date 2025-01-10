package it.cloud37.mendix.exporter.cats;

import java.util.List;

import org.springframework.stereotype.Service;

import it.cloud37.mendix.exporter.humans.HumanDto;

@Service
public class CatService {

    private final CatRepository catRepository;

    public CatService(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    public List<CatDto> getCats() {
        return catRepository.findAll().stream()
                .map(catEntity -> {
                    HumanDto puppet = new HumanDto(catEntity.getHumanPuppet().getId(),
                            catEntity.getHumanPuppet().getName());
                    return new CatDto(
                            catEntity.getId(),
                            catEntity.getName(),
                            catEntity.getAge(),
                            catEntity.getColor(),
                            puppet);
                })
                .toList();
    }

}
