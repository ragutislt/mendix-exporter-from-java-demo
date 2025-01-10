package it.cloud37.java_to_mendix.humans;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="HUMAN")
public class Human {
   @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id; 

    private String name;

    protected Human() {}

    public Human(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}
