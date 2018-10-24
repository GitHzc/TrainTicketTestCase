package org.services.test.entity.dto;

import java.io.Serializable;

public class RefundResponseDto implements Serializable {

    private static final long serialVersionUID = 235856975952199936L;

    private boolean status;

    private String message;

    private String refund;

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

    public String getRefund() {
        return refund;
    }

    public void setRefund(String refund) {
        this.refund = refund;
    }
}
