package org.services.test.entity.dto;

public class ConsignInsertResponseDto {
    private boolean status;

    private String message;

    public ConsignInsertResponseDto(){

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
