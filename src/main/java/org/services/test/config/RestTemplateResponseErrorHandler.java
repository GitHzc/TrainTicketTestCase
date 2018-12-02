package org.services.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.entity.ErrorBody;
import org.services.test.exception.InstanceFaultException;
import org.services.test.exception.UnknownException;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Component
public class RestTemplateResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse clientHttpResponse)
            throws IOException {
        return (clientHttpResponse.getStatusCode().series() == CLIENT_ERROR
                || clientHttpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ErrorBody errorBody = new ErrorBody();
        try {
            errorBody = objectMapper.readValue(
                    new BufferedReader(new InputStreamReader(httpResponse.getBody())), ErrorBody.class);
        } catch (Exception e) {
            throw new UnknownException("unknown error: errorBody");
        }

        if ("java.lang.IndexOutOfBoundsException".equals(errorBody.getException())) {
            throw new UnknownException("instance : IndexOutOfBoundsException");
        }
        if ("java.lang.NullPointerException".equals(errorBody.getException())) {
            throw new UnknownException("instance : NullPointerException");
        }
        if ("java.lang.ArrayIndexOutOfBoundsException".equals(errorBody.getException())) {
            throw new UnknownException("instance : ArrayIndexOutOfBoundsException");
        }

        if (httpResponse.getStatusCode().series() == CLIENT_ERROR
                || httpResponse.getStatusCode().series() == SERVER_ERROR) {
            throw new UnknownException("unknown error: 300 400 500 error");
        }
    }
}
