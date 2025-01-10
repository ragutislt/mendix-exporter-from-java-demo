package it.cloud37.mendix.exporter.export;

import java.util.List;

public record MendixEntity(
        String name,
        List<MendixAttribute> attributes) {
}
