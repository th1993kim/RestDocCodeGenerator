package com.codingdreamtree.RestDocCodeGenerator.dto;

import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import com.codingdreamtree.RestDocCodeGenerator.property.MappingMethodProperty;

import java.util.List;



public class RequestMappingProperty {
    private String requestPath;
    private MappingMethodProperty mappingMethodProperty;
    private List<PsiParameter> pathVariables;
    private List<PsiParameter> requestParameters;
    private PsiParameter requestBody;
    private PsiParameter modelAttribute;
    private PsiType returnType;

    public RequestMappingProperty(String requestPath, MappingMethodProperty mappingMethodProperty, List<PsiParameter> pathVariables, List<PsiParameter> requestParameters, PsiParameter requestBody, PsiParameter modelAttribute, PsiType returnType) {
        this.requestPath = requestPath;
        this.mappingMethodProperty = mappingMethodProperty;
        this.pathVariables = pathVariables;
        this.requestParameters = requestParameters;
        this.requestBody = requestBody;
        this.modelAttribute = modelAttribute;
        this.returnType = returnType;
    }

    public RequestMappingProperty() {
    }

    public static RequestMappingPropertyBuilder builder() {
        return new RequestMappingPropertyBuilder();
    }

    public String getRequestPath() {
        return this.requestPath;
    }

    public MappingMethodProperty getMappingMethodProperty() {
        return this.mappingMethodProperty;
    }

    public List<PsiParameter> getPathVariables() {
        return this.pathVariables;
    }

    public List<PsiParameter> getRequestParameters() {
        return this.requestParameters;
    }

    public PsiParameter getRequestBody() {
        return this.requestBody;
    }

    public PsiParameter getModelAttribute() {
        return this.modelAttribute;
    }

    public PsiType getReturnType() {
        return this.returnType;
    }

    public static class RequestMappingPropertyBuilder {
        private String requestPath;
        private MappingMethodProperty mappingMethodProperty;
        private List<PsiParameter> pathVariables;
        private List<PsiParameter> requestParameters;
        private PsiParameter requestBody;
        private PsiParameter modelAttribute;
        private PsiType returnType;

        RequestMappingPropertyBuilder() {
        }

        public RequestMappingPropertyBuilder requestPath(String requestPath) {
            this.requestPath = requestPath;
            return this;
        }

        public RequestMappingPropertyBuilder mappingMethodProperty(MappingMethodProperty mappingMethodProperty) {
            this.mappingMethodProperty = mappingMethodProperty;
            return this;
        }

        public RequestMappingPropertyBuilder pathVariables(List<PsiParameter> pathVariables) {
            this.pathVariables = pathVariables;
            return this;
        }

        public RequestMappingPropertyBuilder requestParameters(List<PsiParameter> requestParameters) {
            this.requestParameters = requestParameters;
            return this;
        }

        public RequestMappingPropertyBuilder requestBody(PsiParameter requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public RequestMappingPropertyBuilder modelAttribute(PsiParameter modelAttribute) {
            this.modelAttribute = modelAttribute;
            return this;
        }

        public RequestMappingPropertyBuilder returnType(PsiType returnType) {
            this.returnType = returnType;
            return this;
        }

        public RequestMappingProperty build() {
            return new RequestMappingProperty(this.requestPath, this.mappingMethodProperty, this.pathVariables, this.requestParameters, this.requestBody, this.modelAttribute, this.returnType);
        }

        public String toString() {
            return "RequestMappingProperty.RequestMappingPropertyBuilder(requestPath=" + this.requestPath + ", mappingMethodProperty=" + this.mappingMethodProperty + ", pathVariables=" + this.pathVariables + ", requestParameters=" + this.requestParameters + ", requestBody=" + this.requestBody + ", modelAttribute=" + this.modelAttribute + ", returnType=" + this.returnType + ")";
        }
    }
}