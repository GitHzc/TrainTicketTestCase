package org.services.test.entity.dto;

import java.io.Serializable;

public class CancelOrderRequestDto implements Serializable {
    private static final long serialVersionUID = 1821323535584754522L;

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
