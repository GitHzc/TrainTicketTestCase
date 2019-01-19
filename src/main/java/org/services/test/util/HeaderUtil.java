package org.services.test.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

public class HeaderUtil {
    public static final String REQUEST_TYPE_HEADER = "request-type";

    public static HttpHeaders setHeader(Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        for (Map.Entry entry : headers.entrySet()) {
            List<String> values = (List<String>) entry.getValue();
            for (String value : values) {
                httpHeaders.add(entry.getKey().toString(), value);
            }
        }

        return httpHeaders;
    }
}
