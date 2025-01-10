package it.cloud37.java_to_mendix.mendixModel;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.util.AnnotatedTypeScanner;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import it.cloud37.java_to_mendix.JavaToMendixApplication;
import it.cloud37.java_to_mendix.mendixModel.MendixAttribute.AssociationType;
import it.cloud37.java_to_mendix.mendixModel.MendixAttribute.AttributeType;
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

    private static void writeToJsonFile(String filePath, List<MendixEntity> mendixEntities)
            throws IOException, StreamWriteException, DatabindException {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        Path path = Path.of(filePath);
        Files.deleteIfExists(path);
        mapper.writeValue(Files.createFile(path).toFile(), mendixEntities);
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
}
