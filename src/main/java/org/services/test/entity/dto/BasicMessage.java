package org.services.test.entity.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class BasicMessage implements Serializable {
    private static final long serialVersionUID = -796567271948571488L;

    private boolean status;
    private String message;
    private Map<String, List<String>> headers;


    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
