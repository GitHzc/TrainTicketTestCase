package org.services.test.entity.dto;

import java.io.Serializable;

public class RefundRequestDto implements Serializable {
    private static final long serialVersionUID = 7663223018613180894L;
    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
