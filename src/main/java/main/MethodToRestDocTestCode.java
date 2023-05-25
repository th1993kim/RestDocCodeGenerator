package main;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import main.dto.RequestMappingProperty;
import main.property.MappingAnnotation;
import main.property.MappingMethodProperty;
import main.property.ParameterAnnotation;
import main.util.CommonGenerateRestDocUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static main.property.MappingAnnotation.*;
import static main.property.MappingMethodProperty.*;

public class MethodToRestDocTestCode extends AnAction {

    private final Map<MappingAnnotation, MappingMethodProperty> MAPPING_METHOD_PROPERTIES = Map.of(
            GET_MAPPING, GET,
            POST_MAPPING, POST,
            PATCH_MAPPING, PATCH,
            PUT_MAPPING, PUT,
            DELETE_MAPPING, DELETE
    );

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            return;
        }
        PsiElement currentCaretElement = psiFile.findElementAt(editor.getCaretModel().getOffset());

        if (isNotAvailable(currentCaretElement)) {
            return;
        }

        invoke(currentCaretElement);
    }

    private boolean isNotAvailable(PsiElement element) {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) {
            return false;
        }
        PsiModifierList modifierList = method.getModifierList();
        PsiAnnotation[] annotations = modifierList.getAnnotations();
        return Arrays.stream(annotations)
                .noneMatch(
                        annotation -> isMappingAnnotation(
                                StringUtil.defaultIfEmpty(getAnnotationReferenceName(annotation), null)));

    }

    public void invoke(PsiElement element) {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) return;
        PsiModifierList modifierList = method.getModifierList();
        PsiAnnotation[] annotations = modifierList.getAnnotations();
        PsiAnnotation psiMappingAnnotation = getPsiMappingAnnotation(annotations);

        if (psiMappingAnnotation != null) {
            final MappingAnnotation mappingAnnotation = getMappingAnnotation(psiMappingAnnotation);
            final String requestPath = getRequestPath(psiMappingAnnotation);
            final MappingMethodProperty mappingMethodProperty = getMappingMethodProperty(psiMappingAnnotation, mappingAnnotation);
            final PsiParameter[] parameters = method.getParameterList().getParameters();
            final List<PsiParameter> pathVariables = getPathVariables(parameters);
            final List<PsiParameter> requestParameters = getRequestParameters(parameters);
            final PsiParameter requestBody = getRequestBody(parameters);
            final PsiParameter modelAttribute = getModelAttribute(parameters);

            PsiType returnType = method.getReturnType();
            if (returnType != null) {
                String returnTypeName = ((PsiClassReferenceType) returnType).getName();
                if ("ResponseEntity".equals(returnTypeName) || "Page".equals(returnTypeName)){
                    returnType = getGenericType((PsiClassType) returnType);
                }
            }

            if (mappingMethodProperty != null) {
                String generatedRestDoc = CommonGenerateRestDocUtil.initRestDoc(RequestMappingProperty.builder()
                        .requestPath(requestPath)
                        .mappingMethodProperty(mappingMethodProperty)
                        .pathVariables(pathVariables)
                        .requestParameters(requestParameters)
                        .requestBody(requestBody)
                        .modelAttribute(modelAttribute)
                        .returnType(returnType)
                        .build());
                Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                systemClipboard.setContents(new StringSelection(generatedRestDoc), null);
            }
        }
    }

    private MappingMethodProperty getMappingMethodProperty(PsiAnnotation psiMappingAnnotation, MappingAnnotation mappingAnnotation) {
        if (REQUEST_MAPPING == mappingAnnotation) {
            return getMappingMethodPropertyFromRequestMapping(psiMappingAnnotation);
        }
        return MAPPING_METHOD_PROPERTIES.get(mappingAnnotation);
    }

    @Nullable
    private PsiAnnotation getPsiMappingAnnotation(PsiAnnotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(annotation -> isMappingAnnotation(getAnnotationReferenceName(annotation)))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private PsiParameter getModelAttribute(PsiParameter[] parameters) {
        return Arrays.stream(parameters)
                .filter(psiParameter -> Arrays.stream(psiParameter.getAnnotations())
                        .anyMatch(annotation -> ParameterAnnotation.isModelAttributeAnnotation(getAnnotationReferenceName(annotation))))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    private PsiParameter getRequestBody(PsiParameter[] parameters) {
        return Arrays.stream(parameters)
                .filter(psiParameter -> Arrays.stream(psiParameter.getAnnotations())
                        .anyMatch(annotation -> ParameterAnnotation.isRequestBodyAnnotation(getAnnotationReferenceName(annotation))))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    private List<PsiParameter> getRequestParameters(PsiParameter[] parameters) {
        return Arrays.stream(parameters)
                .filter(psiParameter -> Arrays.stream(psiParameter.getAnnotations())
                        .anyMatch(annotation -> ParameterAnnotation.isRequestParamAnnotation(getAnnotationReferenceName(annotation))))
                .collect(Collectors.toList());
    }

    @NotNull
    private List<PsiParameter> getPathVariables(PsiParameter[] parameters) {
        return Arrays.stream(parameters)
                .filter(psiParameter -> Arrays.stream(psiParameter.getAnnotations())
                        .anyMatch(annotation -> ParameterAnnotation.isPathVariableAnnotation(getAnnotationReferenceName(annotation))))
                .collect(Collectors.toList());
    }

    @Nullable
    private String getAnnotationReferenceName(PsiAnnotation annotation) {
        if (annotation.getNameReferenceElement() == null) {
            return null;
        }

        return annotation.getNameReferenceElement().getReferenceName() ;
    }

    private MappingMethodProperty getMappingMethodPropertyFromRequestMapping(PsiAnnotation psiMappingAnnotation) {
        PsiAnnotationMemberValue methodValue = psiMappingAnnotation.findAttributeValue("method");
        if (methodValue != null) {
            return MappingMethodProperty.toMappingMethodProperty(methodValue.getText());
        }
        return GET;

    }

    @Nullable
    private MappingAnnotation getMappingAnnotation(PsiAnnotation psiMappingAnnotation) {
        return Arrays.stream(MappingAnnotation.values())
                .filter(mappingAnnotationValue -> isMappingAnnotation(getAnnotationReferenceName(psiMappingAnnotation)))
                .findFirst()
                .orElse(null);
    }

    private String getRequestPath(PsiAnnotation psiMappingAnnotation) {
        String requestPath = "";
        PsiAnnotationMemberValue pathValue = psiMappingAnnotation.findAttributeValue("value");
        if (pathValue != null) {
            requestPath = pathValue.getText();
        }
        return requestPath;
    }

    @Nullable
    private PsiType getGenericType(PsiClassType classType) {
        return classType.resolveGenerics()
                .getSubstitutor()
                .getSubstitutionMap()
                .values()
                .stream()
                .findFirst()
                .orElse(null);
    }
}
