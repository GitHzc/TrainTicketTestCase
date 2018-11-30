package org.services.test.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class K8sUtil {

    public static String getK8sImageByService(String serviceName) throws KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException, IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " +
                "eyJhbGciOiJSUzI1NiIsImtpZCI6IiJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlLXN5c3RlbSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJhZG1pbi10b2tlbi1uejg2ZyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJhZG1pbiIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjhhNDRhZmEzLWY0NzUtMTFlOC1hMTA2LTAwNTA1NmE0Zjk3OCIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDprdWJlLXN5c3RlbTphZG1pbiJ9.OATqVm8smlGZEIfwlD0spK2Kldg4LMmkCNpLa5WPl8n0f5VTYxV5AhISDvAcbyn4mGNBhbP5d4AoUnc7AIoTnf_JXJxD7e8w6Sw_3er0N3O9O9zHt7tGTywO3lM0hZPq64X8AX6_ARVOpZILTdegQPDPIqHborKoBlMtU0LkXrdSusVtyyxcbYaD2h32m4VmGFck4RvtLpahyMK89iFNPf9YHq5e_A3d_Bz9tO-fzIimAo7EEnpwuc8EJVN25uiU-ZSr5mLtJjgLA1BwyDOJnthpOGImq4nd2yIPuMp_xL_5JaJkZkKk4gIHYtMQXAF0IavyhF5oKj0PE_ItBjtIsA");

        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpEntity entity = new HttpEntity(headers);


        ResponseEntity<String> responseEntity = restTemplate.exchange
                ("https://10.141.212.146:6443/apis/apps/v1/namespaces/default/deployments/ts-execute-service",
                        HttpMethod.GET, entity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode obj = objectMapper.readTree(responseEntity.getBody());

        String image = obj.get("spec").get("template").get("spec").get("containers").get(0).get("image").toString();

        return image;

    }
}
