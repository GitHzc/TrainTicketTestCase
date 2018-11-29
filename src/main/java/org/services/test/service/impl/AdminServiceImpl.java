package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.TsServiceTestApplication;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.config.ClusterConfig;

import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.repository.TestCaseRepository;
import org.services.test.repository.TestTraceRepository;
import org.services.test.service.AdminService;
import org.services.test.util.HeaderUtil;
import org.services.test.util.ParamUtil;
import org.services.test.util.UUIDUtil;
import org.services.test.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestTraceRepository testTraceRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;


    @Override
    public ResponseEntity<Contact> adminLogin(AdminLoginInfoDto dto, HttpHeaders httpHeaders) throws Exception {

        HttpEntity<AdminLoginInfoDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/account/adminlogin");
        ResponseEntity<Contact> resp = restTemplate.exchange(url, HttpMethod.POST, req, Contact.class);
        return resp;
    }

    @Override
    public ResponseEntity<GetAllOrderResultDto> getAllOrderResult(String adminId, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<QueryTicketRequestDto> req = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/adminorder/findAll/" + adminId);
        ResponseEntity<GetAllOrderResultDto> ret = restTemplate.exchange(url, HttpMethod.GET, req, GetAllOrderResultDto.class);
        return ret;
    }

    @Override
    public FlowTestResult adminOrderFlow() {
        return null;
    }

//    @Override
//    public FlowTestResult adminOrderFlow() {
//
//
//        List<TestTrace> traces = new ArrayList<>();
//
//        ThreadLocalCache.testTracesThreadLocal.set(traces);
//        ThreadLocalCache.testCaseIdThreadLocal.set(UUIDUtil.generateUUID());
//
//        /******************
//         * 1st step: login
//         *****************/
//        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();
//        LoginResponseDto loginResponseDto = testLogin(loginRequestDto);
//        if (!loginResponseDto.isStatus()) {
//            return new FlowTestResult();
//        }
//        return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
//    }

    public FlowTestResult returnFlowTestResult(List<TestTrace> testTraceList, TestCase testCase) {
        testCaseRepository.save(testCase);
        testTraceRepository.saveAll(testTraceList);
        FlowTestResult bftr = new FlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(testTraceList);
        return bftr;
    }


    private Contact testAdminLogin(AdminLoginInfoDto adminLoginInfoDto) {
        String loginTraceId = UUIDUtil.generateUUID();

        HttpHeaders loginHeaders = new HttpHeaders();

        loginHeaders.add(ServiceConstant.USER_AGENT, ThreadLocalCache.testCaseIdThreadLocal.get() + "_" + loginTraceId);

        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        Contact contact = null;
        try {
            ResponseEntity<Contact> loginResponseDtoResp = adminLogin(adminLoginInfoDto, loginHeaders);
            contact = loginResponseDtoResp.getBody();
        } catch (Exception e) {

        }

        TestTrace testTrace = new TestTrace();
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-sso-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setExpected_result(0);
        testTrace.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace.setTestClass("BookingFlowTestClass");
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(loginTraceId);
        try {
            testTrace.setReq_param(objectMapper.writeValueAsString(adminLoginInfoDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // todo
        //ts-login-service
        testTrace.setError(0);
        testTrace.setY_issue_ms("ts-login-service");
        testTrace.setY_issue_dim_type("");
        testTrace.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace);
        System.out.println("------- " + testTrace);
        return contact;
    }


}
