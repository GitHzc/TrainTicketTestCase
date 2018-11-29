package org.services.test.entity.dto;

import java.io.Serializable;

public class AdminLoginInfoDto  extends BasicMessage implements Serializable {
    private String name;

    private String password;

    public AdminLoginInfoDto(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
