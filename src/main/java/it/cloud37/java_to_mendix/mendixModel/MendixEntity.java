package it.cloud37.java_to_mendix.mendixModel;

import java.util.List;

public record MendixEntity(
                String name,
                List<MendixAttribute> attributes) {
}
