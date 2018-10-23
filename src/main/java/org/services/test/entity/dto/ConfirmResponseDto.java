package org.services.test.entity.dto;

import java.io.Serializable;

public class ConfirmResponseDto extends BasicMessage implements Serializable {
    private static final long serialVersionUID = 2724046971538211547L;

    private Order order;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
