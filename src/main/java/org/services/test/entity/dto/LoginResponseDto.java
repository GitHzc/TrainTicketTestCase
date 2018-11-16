package org.services.test.entity.dto;

import java.io.Serializable;

public class LoginResponseDto extends BasicMessage implements Serializable{
    private static final long serialVersionUID = 220620653509587488L;

    private boolean status;

    private String message;

    private Account account;
    private String token;

    public LoginResponseDto() {
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
