package org.services.test.entity.dto;

public class InitCacheDataResponse {

    private boolean status;
    private String msg;

    public InitCacheDataResponse() {
    }

    public InitCacheDataResponse(boolean status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
