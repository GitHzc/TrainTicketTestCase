package org.services.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.entity.ErrorBody;
import org.services.test.exception.ConfigFaultException;
import org.services.test.exception.SeqFaultException;
import org.services.test.exception.UnknownException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Component
public class RestTemplateResponseErrorHandler
        implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {
        // Ignore exception
        return (
                httpResponse.getStatusCode().series() == CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {

//        String result = new BufferedReader(new InputStreamReader(httpResponse.getBody()))
//                .lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        ErrorBody errorBody = null;
        try {
            errorBody = objectMapper.readValue(
                    new BufferedReader(new InputStreamReader(httpResponse.getBody())), ErrorBody.class);
        }
        catch (Exception e) {
            if (HttpStatus.SERVICE_UNAVAILABLE == httpResponse.getStatusCode()) {
                throw new ConfigFaultException("memory error");
            }

        }

        if ("java.lang.IndexOutOfBoundsException".equals(errorBody.getException())) {
            throw new SeqFaultException(errorBody, "seq error");
        }
        else if ("org.springframework.web.client.HttpServerErrorException".equals(errorBody.getException()))
        {
            throw new ConfigFaultException(errorBody, "memory error");
        }
        else if ("java.net.SocketTimeoutException".equals(errorBody.getException())) {
            throw new ConfigFaultException(errorBody, "cpu error");
        }

        if (httpResponse.getStatusCode().series() == CLIENT_ERROR
                || httpResponse.getStatusCode().series() == SERVER_ERROR) {
            throw new UnknownException("unknown error");
        }
    }
}
