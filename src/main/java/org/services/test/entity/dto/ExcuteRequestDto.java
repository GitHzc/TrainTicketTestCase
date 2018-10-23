package org.services.test.entity.dto;

import java.io.Serializable;

public class ExcuteRequestDto implements Serializable {
    private static final long serialVersionUID = -1703734080244278869L;

    private String orderId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
