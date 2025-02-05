package it.cloud37.mendix.exporter.export;

import java.util.List;

record MendixEntity(
        String name,
        List<MendixAttribute> attributes) {
}
