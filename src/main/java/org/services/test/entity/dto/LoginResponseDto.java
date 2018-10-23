package org.services.test.entity.dto;

import java.io.Serializable;

public class LoginResponseDto extends BasicMessage implements Serializable{
    private static final long serialVersionUID = 220620653509587488L;

    private Account account;
    private String token;

    public LoginResponseDto() {
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
