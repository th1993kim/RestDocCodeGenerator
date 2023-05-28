package com.codingdreamtree.RestDocCodeGenerator.property;

public enum ParameterAnnotation {
    PATH_VARIABLE("PathVariable"),
    REQUEST_PARAM("RequestParam"),
    REQUEST_BODY("RequestBody"),
    MODEL_ATTRIBUTE("ModelAttribute");


    private final String annotationName;

    ParameterAnnotation(String annotationName) {
        this.annotationName = annotationName;
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public static boolean isPathVariableAnnotation(String name) {
        return PATH_VARIABLE.getAnnotationName().equals(name);
    }

    public static boolean isRequestParamAnnotation(String name) {
        return REQUEST_PARAM.getAnnotationName().equals(name);
    }

    public static boolean isRequestBodyAnnotation(String name) {
        return REQUEST_BODY.getAnnotationName().equals(name);
    }

    public static boolean isModelAttributeAnnotation(String name) {
        return MODEL_ATTRIBUTE.getAnnotationName().equals(name);
    }
}
