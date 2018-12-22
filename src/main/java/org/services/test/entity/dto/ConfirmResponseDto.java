package org.services.test.entity.dto;

import java.io.Serializable;

public class ConfirmResponseDto extends BasicMessage implements Serializable {
    private static final long serialVersionUID = 2724046971538211547L;

    private boolean status;

    private String message;

    private Order order;

    public ConfirmResponseDto() {
    }

    public ConfirmResponseDto(boolean status, String message, Order order) {
        this.status = status;
        this.message = message;
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }



    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean isStatus() {
        return status;
    }

    @Override
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }
}
