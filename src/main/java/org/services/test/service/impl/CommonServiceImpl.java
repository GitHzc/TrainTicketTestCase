package org.services.test.service.impl;

import org.services.test.config.ClusterConfig;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.QueryTicketRequestDto;
import org.services.test.entity.dto.QueryTicketResponseDto;
import org.services.test.service.CommonService;
import org.services.test.util.HeaderUtil;
import org.services.test.util.ParamUtil;
import org.services.test.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class CommonServiceImpl implements CommonService {
    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseEntity<List<QueryTicketResponseDto>> queryTicket(Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        QueryTicketRequestDto dto = ParamUtil.constructQueryTicketReqDto();

        HttpEntity<QueryTicketRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String uri = null;
        if (dto.getEndPlace().equals(ServiceConstant.NAN_JING)) {
            uri = "/travel2/query";
        } else {
            uri = "/travel/query";
        }
        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), uri);
        ResponseEntity<List<QueryTicketResponseDto>> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<QueryTicketResponseDto>>() {
                });

        return ret;
    }
}
