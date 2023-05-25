package main.util;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import main.dto.RequestMappingProperty;
import main.property.RestDocType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommonGenerateRestDocUtil {

    public static String initRestDoc(RequestMappingProperty requestMappingProperty) {

        String restDoc = createHeaderRestDoc(requestMappingProperty);
        restDoc += createDocumentRestDoc(requestMappingProperty);
        return restDoc;
    }

    private static String createHeaderRestDoc(RequestMappingProperty requestMappingProperty) {
        final String methodName = requestMappingProperty.getMappingMethodProperty().name().toLowerCase();
        final String requestPath = requestMappingProperty.getRequestPath();
        final List<PsiParameter> pathVariables = requestMappingProperty.getPathVariables();
        final List<PsiParameter> requestParameters = requestMappingProperty.getRequestParameters();
        final PsiParameter modelAttribute = requestMappingProperty.getModelAttribute();
        final PsiParameter requestBody = requestMappingProperty.getRequestBody();
        final StringBuilder sb = new StringBuilder();

        sb.append("mockMvc.perform(")
                .append(methodName)
                .append("(")
                .append(requestPath)
                .append(getPathVariableHeader(pathVariables))
                .append("\n\t.contentType(MediaType.APPLICATION_JSON)");

        if (requestBody != null) {
            sb.append("\n\t.accept(MediaType.APPLICATION_JSON)")
                    .append("\n\t.content(")
                    .append(requestBody.getName())
                    .append(")");
        }

        if (modelAttribute != null) {
            sb.append("\n\t.params()");
        }

        sb.append(getRequestParamHeader(requestParameters))
                .append(")\n")
                .append("\t.andDo(print())\n")
                .append("\t.andExpect(status().isOk())\n");
        String result = sb.toString();
        sb.setLength(0);
        return result;
    }

    private static String getPathVariableHeader(List<PsiParameter> pathVariables) {
        if (pathVariables == null || pathVariables.isEmpty()) {
            return ")";
        }
        return ", " + pathVariables.stream()
                .map(PsiParameter::getName)
                .collect(Collectors.joining(", ")) + ")";
    }

    private static String getRequestParamHeader(List<PsiParameter> requestParameters) {
        if (requestParameters == null || requestParameters.isEmpty()) {
            return "";
        }
        return requestParameters.stream()
                .map(requestParam -> "\n\t.param(\"" + requestParam.getName() + "\", " + requestParam.getName() + ")")
                .collect(Collectors.joining());
    }

    private static String createDocumentRestDoc(RequestMappingProperty requestMappingProperty) {
        final StringBuilder sb = new StringBuilder();
        sb.append("\t.andDo(\n")
                .append("\t\tdocument(\"\",\n")
                .append(generateRequestParameterText(requestMappingProperty.getRequestParameters(), requestMappingProperty.getModelAttribute()))
                .append(generatePathParameterText(requestMappingProperty.getPathVariables()))
                .append(generateRequestFieldText(requestMappingProperty.getRequestBody()))
                .append(generateResponseField(requestMappingProperty.getReturnType()))
                .append("\t\t)\n")
                .append("\t);");

        String result = sb.toString();
        sb.setLength(0);
        return result;
    }

    private static String generateResponseField(PsiType returnType) {
        StringBuilder sb = new StringBuilder();
        PsiClass responseClass = getPsiClass(returnType);
        if (responseClass != null && !"void".equalsIgnoreCase(responseClass.getName())) {
            String restDocResultString = String.join(",\n\t\t\t", generateRecursiveRestDocText(responseClass, getBeforeFieldName(returnType), RestDocType.RESPONSE_BODY));
            sb.append("\t\t\tresponseFields(\n")
                    .append("\t\t\t\t")
                    .append(restDocResultString)
                    .append("\n\t\t\t)\n");
        }
        String result = sb.toString();
        sb.setLength(0);
        return result;
    }

    private static String getBeforeFieldName(PsiType psiType) {
        if (TypeCheckUtil.isCollection(psiType.getCanonicalText())) {
            return "[].";
        }

        return "";
    }

    private static String generateRequestFieldText(PsiParameter requestBody) {
        StringBuilder sb = new StringBuilder();
        if (requestBody != null) {
            PsiType psiType = requestBody.getType();
            PsiClass requestClass = getPsiClass(psiType);
            if (requestClass != null) {
                sb.append("\t\t\trequestFields(\n")
                        .append("\t\t\t\t")
                        .append(String.join(",\n\t\t\t\t\t", generateRecursiveRestDocText(requestClass,
                                getBeforeFieldName(psiType), RestDocType.REQUEST_BODY)))
                        .append("\n\t\t\t),\n");
            }
        }

        String result = sb.toString();
        sb.setLength(0);
        return result;
    }


    private static String generateRequestParameterText(List<PsiParameter> requestParameters, PsiParameter modelAttribute) {
        StringBuilder sb = new StringBuilder();
        if ((requestParameters != null && !requestParameters.isEmpty()) || modelAttribute != null) {
            sb.append("\t\t\trequestParameters(\n")
                    .append("\t\t\t\t")
                    .append(generateRequestParameterWithName(requestParameters))
                    .append(generateRequestParameterWithNameFromModelAttribute(modelAttribute))
                    .append("\n\t\t\t),\n");
        }
        String result = sb.toString();
        sb.setLength(0);
        return result;
    }

    private static String generateRequestParameterWithNameFromModelAttribute(PsiParameter modelAttribute) {

        if (modelAttribute != null) {
            PsiType psiType = modelAttribute.getType();
            PsiClass modelClass = getPsiClass(psiType);
            if (modelClass != null) {
                return String.join(",\n\t\t\t\t\t", generateRecursiveRestDocText(modelClass, getBeforeFieldName(psiType), RestDocType.MODEL_ATTRIBUTE));
            }
        }

        return "";
    }

    private static String generateRequestParameterWithName(List<PsiParameter> requestParameters) {
        if (requestParameters == null || requestParameters.isEmpty()) {
            return "";
        }

        return requestParameters.stream()
                .map(requestParameter -> "parameterWithName(\"" + requestParameter.getName() + "\").description(\"\")")
                .collect(Collectors.joining("\n"));
    }

    private static String generatePathParameterText(List<PsiParameter> pathVariables) {
        StringBuilder sb = new StringBuilder();
        if (pathVariables != null && !pathVariables.isEmpty()) {
            sb.append("\t\t\tpathParameters(\n")
                    .append("\t\t\t\t")
                    .append(generatePathParametersWithName(pathVariables))
                    .append("\n\t\t\t),\n");
        }
        String result = sb.toString();
        sb.setLength(0);
        return result;
    }

    private static String generatePathParametersWithName(List<PsiParameter> pathVariables) {
        if (pathVariables == null || pathVariables.isEmpty()) {
            return "";
        }

        return pathVariables.stream()
                .map(pathVariable -> "parameterWithName(\"" + pathVariable.getName() + "\").description(\"\")")
                .collect(Collectors.joining(",\n"));
    }


    @Nullable
    private static PsiClass getPsiClass(PsiType returnType) {
        PsiClassType classType = (PsiClassType) returnType;
        if (TypeCheckUtil.isCollection(classType.getCanonicalText())) {
            PsiType parameter = ((PsiClassType) returnType).getParameters()[0];
            return ((PsiClassType) parameter).resolve();
        }
        return classType.resolve();
    }

    private static List<String> generateRecursiveRestDocText(PsiClass psiClass, String beforeFieldName, RestDocType restDocType) {
        List<String> restDocList = new ArrayList<>();
        PsiField[] fields = psiClass.getFields();
        Arrays.stream(fields)
                .forEach(field ->
                        {
                            final PsiType type = field.getType();
                            final String className = type.getCanonicalText();
                            String fieldName = beforeFieldName + field.getName();

                            if (type instanceof PsiPrimitiveType)
                                restDocList.add(generateResponseFieldText(type, fieldName, restDocType));

                            if (type instanceof PsiClassType) {
                                PsiClass resolvedClass = ((PsiClassType) type).resolve();
                                if (isCommonClass(className, resolvedClass)) {
                                    restDocList.add(generateResponseFieldText(type, fieldName, restDocType));

                                } else if (TypeCheckUtil.isCollection(className)) {
                                    PsiType parameter = ((PsiClassType) type).getParameters()[0];
                                    PsiClass genericClass = ((PsiClassType) parameter).resolve();
                                    fieldName += ".[].";
                                    if (genericClass == null) return;
                                    restDocList.addAll(generateRecursiveRestDocText(genericClass, fieldName, restDocType));

                                } else {
                                    if (resolvedClass == null) return;
                                    fieldName += ".";
                                    restDocList.addAll(generateRecursiveRestDocText(resolvedClass, fieldName, restDocType));
                                }
                            }
                        }
                );

        return restDocList;
    }

    private static boolean isCommonClass(String className, PsiClass resolvedClass) {
        return TypeCheckUtil.isCharClass(className)
                || TypeCheckUtil.isNumberClass(className)
                || TypeCheckUtil.isTimeClass(className)
                || TypeCheckUtil.isEnumClass(resolvedClass);
    }

    private static String generateResponseFieldText(PsiType type, String fieldName, RestDocType restDocType) {
        if (restDocType.isResponseBody()) {
            return "fieldWithPath(\"" + fieldName + "\").type(" + createJsonReturnType(type) + ").description(\"\")";
        }
        if (restDocType.isRequestBody()) {
            return "fieldWithPath(\"" + fieldName + "\").type(" + createJsonReturnType(type) + ").description(\"\")";
        }
        if (restDocType.isModelAttribute()) {
            return "parameterWithName(\"" + fieldName + "\").description(\"\")";
        }
        return "";
    }


    private static String createJsonReturnType(PsiType type) {
        final String className = type.getCanonicalText();
        if (TypeCheckUtil.isNumberClass(className)) {
            return "JsonFieldType.NUMBER";
        }
        if (TypeCheckUtil.isBooleanClass(className)) {
            return "JsonFieldType.BOOLEAN";
        }
        return "JsonFieldType.STRING";
    }
}
