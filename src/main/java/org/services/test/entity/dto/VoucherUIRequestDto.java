package org.services.test.entity.dto;

import java.io.Serializable;

public class VoucherUIRequestDto implements Serializable {

    private static final long serialVersionUID = 5221972382955207651L;

    private String orderId;

    private String train_number;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTrain_number() {
        return train_number;
    }

    public void setTrain_number(String train_number) {
        this.train_number = train_number;
    }
}
