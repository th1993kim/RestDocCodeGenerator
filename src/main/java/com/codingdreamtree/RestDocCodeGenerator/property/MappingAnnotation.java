package com.codingdreamtree.RestDocCodeGenerator.property;

import java.util.Arrays;

public enum MappingAnnotation {
    GET_MAPPING("GetMapping"),
    POST_MAPPING("PostMapping"),
    REQUEST_MAPPING("RequestMapping"),
    DELETE_MAPPING("DeleteMapping"),
    PUT_MAPPING("PutMapping"),
    PATCH_MAPPING("PatchMapping");

    private final String realName;

    MappingAnnotation(String realName) {
        this.realName = realName;
    }

    public String getRealName() {
        return realName;
    }

    public static boolean isMappingAnnotation(String annotationName) {
        return Arrays.stream(MappingAnnotation.values())
                .anyMatch(annotation -> annotation.getRealName().equals(annotationName));
    }
}
