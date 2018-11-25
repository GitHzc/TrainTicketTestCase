package org.services.test.exception;

import org.services.test.entity.ErrorBody;

public class SeqFaultException extends RuntimeException {
    private static final long serialVersionUID = 112051467642422912L;

    private ErrorBody errorBody;

    public SeqFaultException(String msg) {
        super(msg);
    }

    public SeqFaultException(ErrorBody errorBody, String msg) {
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
