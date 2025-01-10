package it.cloud37.mendix.exporter.export;

import java.util.Map;

public record MendixAttribute(
        String name,
        AttributeType type,
        AssociationType associationType,
        String entityType) {

    public enum AttributeType {
        STRING,
        INTEGER,
        DECIMAL,
        AUTO_NUMBER,
        BOOLEAN,
        ENUM,
        ENTITY;
    }

    public enum AssociationType {
        ONE_TO_ONE,
        ONE_TO_MANY
    }

    private static final Map<Class<?>, AttributeType> JAVA_TO_MENDIX_TYPE = Map.ofEntries(
            Map.entry(String.class, AttributeType.STRING),
            Map.entry(Integer.class, AttributeType.INTEGER),
            Map.entry(int.class, AttributeType.INTEGER),
            Map.entry(Long.class, AttributeType.INTEGER),
            Map.entry(long.class, AttributeType.INTEGER),
            Map.entry(Float.class, AttributeType.DECIMAL),
            Map.entry(float.class, AttributeType.DECIMAL),
            Map.entry(Double.class, AttributeType.DECIMAL),
            Map.entry(double.class, AttributeType.DECIMAL),
            Map.entry(Boolean.class, AttributeType.BOOLEAN),
            Map.entry(boolean.class, AttributeType.BOOLEAN),
            Map.entry(Enum.class, AttributeType.ENUM));

    public static AttributeType getMendixType(Class<?> javaType) {
        return JAVA_TO_MENDIX_TYPE.getOrDefault(javaType, AttributeType.ENTITY);
    }
}