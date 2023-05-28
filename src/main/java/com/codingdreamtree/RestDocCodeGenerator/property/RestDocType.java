package com.codingdreamtree.RestDocCodeGenerator.property;

public enum RestDocType {
    MODEL_ATTRIBUTE,
    REQUEST_BODY,
    RESPONSE_BODY;


    public boolean isModelAttribute() {
        return MODEL_ATTRIBUTE == this;
    }

    public boolean isRequestBody() {
        return REQUEST_BODY == this;
    }

    public boolean isResponseBody() {
        return RESPONSE_BODY == this;
    }
}
