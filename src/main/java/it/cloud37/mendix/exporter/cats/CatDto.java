package it.cloud37.mendix.exporter.cats;

import it.cloud37.mendix.exporter.humans.HumanDto;

public record CatDto(long id, String name, int age, String color, HumanDto humanPuppet) {

}
