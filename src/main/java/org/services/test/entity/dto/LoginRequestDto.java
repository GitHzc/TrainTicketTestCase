package org.services.test.entity.dto;

import java.io.Serializable;

public class LoginRequestDto implements Serializable{

    private static final long serialVersionUID = 4096776124936811856L;

    private String email;
    private String password;
    private String verificationCode;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}
