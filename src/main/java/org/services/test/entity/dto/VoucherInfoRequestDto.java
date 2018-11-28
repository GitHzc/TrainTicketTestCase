package org.services.test.entity.dto;

import java.io.Serializable;

public class VoucherInfoRequestDto implements Serializable {

    private static final long serialVersionUID = -8731137952059925167L;

    private String orderId;

    private int type;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
