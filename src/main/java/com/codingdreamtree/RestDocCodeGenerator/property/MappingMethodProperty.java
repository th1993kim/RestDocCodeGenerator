package com.codingdreamtree.RestDocCodeGenerator.property;

import java.util.Arrays;

public enum MappingMethodProperty {
    GET, POST, PUT, DELETE, PATCH;

    public static MappingMethodProperty toMappingMethodProperty(String propertyName) {
        return Arrays.stream(MappingMethodProperty.values())
                .filter(mappingMethodProperty -> mappingMethodProperty.name().equalsIgnoreCase(propertyName.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}
