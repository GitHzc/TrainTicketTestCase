package org.services.test.entity.dto;

public class DeleteOrderResultDto {
    public boolean status;

    public String message;

    public DeleteOrderResultDto() {
        //Default Constructor
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
