package it.cloud37.mendix.exporter.export;

import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

record MendixPublishedRestServiceOperation(String path, RestOperation restOperation) {
    enum RestOperation {
        GET,
        POST,
        PUT,
        DELETE
    }

    private static final Map<Class<?>, RestOperation> JAVA_TO_MENDIX_REST_OPERATION = Map.ofEntries(
            Map.entry(GetMapping.class, RestOperation.GET),
            Map.entry(PostMapping.class, RestOperation.POST),
            Map.entry(PutMapping.class, RestOperation.PUT),
            Map.entry(DeleteMapping.class, RestOperation.DELETE));

    static RestOperation getMendixRestOperation(Class<?> javaType) {
        return JAVA_TO_MENDIX_REST_OPERATION.get(javaType);
    }
}
