package org.services.test.entity;

import java.io.Serializable;

public class VoucherRequestDto implements Serializable {

    private static final long serialVersionUID = -6251046717307382557L;

    private String orderId;
    private int type;

    public VoucherRequestDto(String orderId, int type) {
        this.orderId = orderId;
        this.type = type;
    }

    public VoucherRequestDto() {
    }

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
