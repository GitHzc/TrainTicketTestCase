package org.services.test.exception;

import org.services.test.entity.ErrorBody;

import java.io.Serializable;

public class ConfigFaultException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 112061467642422912L;

    private ErrorBody errorBody;

    public ConfigFaultException(String msg) {
        super(msg);
    }

    public ConfigFaultException(ErrorBody errorBody, String msg) {
        super(msg);
        this.errorBody = errorBody;
    }

    public ErrorBody getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(ErrorBody errorBody) {
        this.errorBody = errorBody;
    }
}
