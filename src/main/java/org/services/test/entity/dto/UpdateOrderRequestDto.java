package org.services.test.entity.dto;

public class UpdateOrderRequestDto {

    private String loginid;
    private Order order;

    public UpdateOrderRequestDto(){

    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

}
