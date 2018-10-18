package org.services.test.service.impl;

import org.services.test.config.ClusterConfig;
import org.services.test.entity.dto.LoginRequestDto;
import org.services.test.entity.dto.LoginResponseDto;
import org.services.test.service.BookingFlowService;
import org.services.test.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BookingFlowServiceImpl implements BookingFlowService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

    @Override
    public LoginResponseDto login() {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail("fdse_microservices@163.com");
        dto.setPassword("DefaultPassword");
        dto.setVerificationCode("abcd");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", "YsbCaptcha=56A259F4BE35450FA8D8C352744CF38A");
        HttpEntity<LoginRequestDto> req = new HttpEntity<>(dto, headers);

        ResponseEntity<LoginResponseDto> resp = restTemplate.exchange(
                UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/login"),
                HttpMethod.POST, req, LoginResponseDto.class);
        return resp.getBody();
    }
}
