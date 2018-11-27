package org.services.test.exception;

import org.services.test.entity.ErrorBody;

public class UnknownException extends RuntimeException {
    private static final long serialVersionUID = 1329907929493355904L;

    private ErrorBody errorBody;

    public UnknownException(String msg) {
        super(msg);
    }

    public UnknownException(ErrorBody errorBody, String msg) {
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
