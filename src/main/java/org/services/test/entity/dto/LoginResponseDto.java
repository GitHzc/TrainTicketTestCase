package org.services.test.entity.dto;

import java.io.Serializable;

public class LoginResponseDto implements Serializable{
    private static final long serialVersionUID = 220620653509587488L;

    private boolean status;
    private String message;
    private Account account;
    private String token;

    public LoginResponseDto() {
    }

    public LoginResponseDto(boolean status, String message, Account account, String token) {
        this.status = status;
        this.message = message;
        this.account = account;
        this.token = token;
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

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
