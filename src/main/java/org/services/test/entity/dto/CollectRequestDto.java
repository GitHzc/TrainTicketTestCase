package org.services.test.entity.dto;

import java.io.Serializable;

public class CollectRequestDto implements Serializable {
    private static final long serialVersionUID = 3860455535853692463L;

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
