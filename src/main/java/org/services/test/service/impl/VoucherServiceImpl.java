package org.services.test.service.impl;

import org.services.test.config.ClusterConfig;
import org.services.test.entity.dto.VoucherRequestDto;
import org.services.test.entity.dto.VoucherResponseDto;
import org.services.test.service.VoucherService;
import org.services.test.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VoucherServiceImpl implements VoucherService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

    @Override
    public VoucherResponseDto getVoucherInfo() {
        VoucherRequestDto vrd = new VoucherRequestDto("d4a8c3ed-b015-4324-a62f-2592a5450b4a", 1);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<VoucherRequestDto> req = new HttpEntity<>(vrd, headers);
        ResponseEntity<VoucherResponseDto> re = null;
        try {
            re = restTemplate.exchange(
                    UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/getVoucher"),
                    HttpMethod.POST, req, VoucherResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("sql error");
        }
        VoucherResponseDto resp = re.getBody();
        return resp;
    }
}