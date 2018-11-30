package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.AddContactsInfo;
import org.services.test.entity.dto.AddContactsResult;
import org.services.test.entity.dto.QueryTicketRequestDto;
import org.services.test.entity.dto.QueryTicketResponseDto;
import org.services.test.service.CommonService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommonServiceImpl implements CommonService {
    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity<List<QueryTicketResponseDto>> commonQueryTicket(QueryTicketRequestDto dto, Map<String,
            List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);

        HttpEntity<QueryTicketRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String uri;
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

    @Override
    public ResponseEntity<AddContactsResult> commonCreateContact(Map<String, List<String>> headers) {
        AddContactsInfo addContactsInfo = ParamUtil.constructAddContactsInfo();

        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<AddContactsInfo> req = new HttpEntity<>(addContactsInfo, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/contacts/create");
        ResponseEntity<AddContactsResult> ret = restTemplate.exchange(url, HttpMethod.POST, req, AddContactsResult
                .class);
        return ret;
    }

    void testCreateContact(Map<String, List<String>> headers) throws JsonProcessingException {
        String traceId = UUIDUtil.generateUUID();

        TestTrace testTrace = new TestTrace();
        testTrace.setError(0);
        testTrace.setEntryApi("/contacts/create");
        testTrace.setEntryService("ts-contacts-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setExpected_result(0);
        testTrace.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace.setReq_param(objectMapper.writeValueAsString(null));
        testTrace.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace.setTestClass("CommonTestClass");
        testTrace.setTestMethod("getContacts");
        testTrace.setTestTraceId("createContact");
        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                traceId));

        commonCreateContact(headers);
    }

    void testQueryTicket(Map<String, List<String>> headers) throws JsonProcessingException {
        QueryTicketRequestDto queryTicketRequestDto = ParamUtil.constructQueryTicketReqDto();

        String queryTicketTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                queryTicketTraceId));

        TestTrace testTrace2 = new TestTrace();
        testTrace2.setError(0);
        if (queryTicketRequestDto.getEndPlace().equals(ServiceConstant.NAN_JING)) {
            testTrace2.setEntryApi("/travel2/query");
            testTrace2.setEntryService("ts-travel2-service");
        } else {
            testTrace2.setEntryApi("/travel/query");
            testTrace2.setEntryService("ts-travel-service");
        }
        testTrace2.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setExpected_result(0);
        testTrace2.setReq_param(objectMapper.writeValueAsString(queryTicketRequestDto));
        testTrace2.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace2.setTestClass("BookingFlowTestClass");
        testTrace2.setTestMethod("queryTicket");
        testTrace2.setTestTraceId(queryTicketTraceId);
        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace2);

        ResponseEntity<List<QueryTicketResponseDto>> queryTicketResponseDtosResp = commonQueryTicket
                (queryTicketRequestDto,
                        headers);
    }


}
