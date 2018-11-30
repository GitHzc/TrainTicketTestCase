package org.services.test.entity.dto;

import java.io.Serializable;

public class YissueDimDto implements Serializable {

    private static final long serialVersionUID = 7738422339202405122L;

    private String ms;
    private String type;
    private String content;

    public String getMs() {
        return ms;
    }

    public void setMs(String ms) {
        this.ms = ms;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
