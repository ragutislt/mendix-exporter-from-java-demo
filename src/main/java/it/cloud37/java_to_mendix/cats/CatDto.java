package it.cloud37.java_to_mendix.cats;

import it.cloud37.java_to_mendix.humans.HumanDto;

public record CatDto (long id, String name, int age, String color, HumanDto humanPuppet) {
    
}
