package org.services.test.entity.dto;

import java.io.Serializable;

public class PaymentRequestDto implements Serializable {

    private static final long serialVersionUID = 4062103121498782410L;

    private String orderId;
    private String tripId;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
