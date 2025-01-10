package it.cloud37.mendix.exporter.cats;

import it.cloud37.mendix.exporter.humans.Human;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CAT")
class Cat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    protected Cat() {
    }

    public Cat(String name, int age, String color, Human humanPuppet) {
        this.name = name;
        this.age = age;
        this.color = color;
        this.humanPuppet = humanPuppet;
    }

    private String name;

    private int age;
    private String color;

    @OneToOne
    private Human humanPuppet;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getColor() {
        return color;
    }

    public Human getHumanPuppet() {
        return humanPuppet;
    }
}