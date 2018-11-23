package org.services.test.config;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class RestTemplateResponseErrorHandler
        implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        // Ignore exception
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {

        // nothing to do
    }
}
