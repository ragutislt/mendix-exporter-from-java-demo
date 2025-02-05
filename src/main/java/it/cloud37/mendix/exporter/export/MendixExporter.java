package it.cloud37.mendix.exporter.export;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.data.util.AnnotatedTypeScanner;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.cloud37.mendix.exporter.JavaToMendixApplication;
import it.cloud37.mendix.exporter.export.MendixAttribute.AssociationType;
import it.cloud37.mendix.exporter.export.MendixAttribute.AttributeType;
import it.cloud37.mendix.exporter.export.MendixPublishedRestServiceOperation.RestOperation;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MendixExporter {
    public static void exportEntitiesTo(String filePath) throws IOException {
        AnnotatedTypeScanner typeScanner = new AnnotatedTypeScanner(false, Entity.class);

        Set<Class<?>> entityClasses = typeScanner.findTypes(JavaToMendixApplication.class.getPackageName());
        log.info("Entity classes are: {}", entityClasses);

        List<MendixEntity> mendixEntities = new ArrayList<>();

        for (Class<?> entityClass : entityClasses) {
            List<MendixAttribute> attributes = new ArrayList<>();
            for (Field field : entityClass.getDeclaredFields()) {

                AttributeType attributeType = determineAttributeType(field);
                AssociationType associationType = determineAssociationType(field, attributeType);
                String associationEntityType = determineAssociationEntityType(field, attributeType);

                attributes.add(
                        new MendixAttribute(field.getName(), attributeType, associationType, associationEntityType));
            }
            MendixEntity newEntity = new MendixEntity(entityClass.getSimpleName(), attributes);
            mendixEntities.add(newEntity);
        }

        writeToJsonFile(filePath, mendixEntities);
    }

    private static void writeToJsonFile(String filePath, Object mendixObjects)
            throws IOException, StreamWriteException, DatabindException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Path path = Path.of(filePath);
        Files.deleteIfExists(path);
        mapper.writeValue(Files.createFile(path).toFile(), mendixObjects);
    }

    private static String determineAssociationEntityType(Field field, AttributeType attributeType) {
        if (attributeType.equals(AttributeType.ENTITY)) {
            return field.getType().getSimpleName();
        }
        return "";
    }

    private static AssociationType determineAssociationType(Field field, AttributeType attributeType) {
        if (!attributeType.equals(AttributeType.ENTITY))
            return null;

        if (field.getType().equals(List.class)) {
            return AssociationType.ONE_TO_MANY;
        } else {
            return AssociationType.ONE_TO_ONE;
        }
    }

    private static AttributeType determineAttributeType(Field field) {
        AttributeType attributeType = MendixAttribute.getMendixType(field.getType());
        if (field.isAnnotationPresent(Id.class)) {
            attributeType = AttributeType.AUTO_NUMBER;
        }
        return attributeType;
    }

    public static void exportRESTLayerTo(String filePath) throws IOException {
        AnnotatedTypeScanner typeScanner = new AnnotatedTypeScanner(false, RestController.class);

        Set<Class<?>> restControllerClasses = typeScanner.findTypes(JavaToMendixApplication.class.getPackageName());
        log.info("Controller classes are: {}", restControllerClasses);

        List<MendixPublishedRestService> mendixRestServices = new ArrayList<>();
        List<MendixPublishedRestServiceResource> mendixResources = new ArrayList<>();

        for (Class<?> restControllerClass : restControllerClasses) {
            List<MendixPublishedRestServiceOperation> mendixOperations = new ArrayList<>();
            for (Method method : restControllerClass.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                Optional<MendixPublishedRestServiceOperation> restOperation = findRestOperation(
                        method.getAnnotations());
                restOperation.ifPresent(op -> mendixOperations.add(op));
            }

            MendixPublishedRestServiceResource newResource = new MendixPublishedRestServiceResource(
                    retrieveControllerPath(restControllerClass), mendixOperations);
            mendixResources.add(newResource);
        }

        mendixRestServices.add(new MendixPublishedRestService("JavaToMendixApplication", "api", "1", mendixResources));
        writeToJsonFile(filePath, mendixRestServices);
    }

    private static String retrieveControllerPath(Class<?> controllerClass) {
        RequestMapping[] requestMapping = controllerClass.getAnnotationsByType(RequestMapping.class);
        String pathValue = requestMapping[0].path()[0];
        if(pathValue.startsWith("/"))
            pathValue = pathValue.replaceFirst("/", "");
        return pathValue;
    }

    private static Optional<MendixPublishedRestServiceOperation> findRestOperation(Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(a -> MendixPublishedRestServiceOperation.getMendixRestOperation(a.annotationType()) != null)
                .findFirst()
                .map(a -> {
                    String path = getAnnotationPathValue(a);
                    RestOperation op = MendixPublishedRestServiceOperation.getMendixRestOperation(a.annotationType());
                    return new MendixPublishedRestServiceOperation(path, op);
                });
    }

    private static String getAnnotationPathValue(Annotation a) {
        List<String> pathValue;
        String path = "";
        try {
            pathValue = Arrays.asList((String[]) a.annotationType().getMethod("path").invoke(a));
            if (!pathValue.isEmpty())
                path = pathValue.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mapping annotations");
        }
        return path;
    }
}
