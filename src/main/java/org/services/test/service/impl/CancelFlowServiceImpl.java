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
import org.services.test.service.CancelFlowService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CancelFlowServiceImpl implements CancelFlowService {
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

    private static ThreadLocal<String> testCaseIdThreadLocal = new ThreadLocal<>();

    private static ThreadLocal<List<TestTrace>> testTracesThreadLocal = new ThreadLocal<>();

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpHeaders httpHeaders) {
        HttpEntity<LoginRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/login");
        ResponseEntity<LoginResponseDto> resp = restTemplate.exchange(url, HttpMethod.POST, req,
                LoginResponseDto.class);

        HttpHeaders responseHeaders = resp.getHeaders();
        List<String> values = responseHeaders.get(ServiceConstant.SET_COOKIE);
        List<String> respCookieValue = new ArrayList<>();
        for (String cookie : values) {
            respCookieValue.add(cookie.split(";")[0]);
        }
        LoginResponseDto ret = resp.getBody();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(ServiceConstant.COOKIE, respCookieValue);
        ret.setHeaders(headers);
        return resp;
    }

    @Override
    public ResponseEntity<List<Order>> queryOrder(OrderQueryRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/order/query");
        ResponseEntity<List<Order>> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<Order>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<List<Order>> queryOrderOther(OrderQueryRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/orderOther/query");
        ResponseEntity<List<Order>> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<Order>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<RefundResponseDto> calculateRefund(RefundRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<RefundRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/cancelCalculateRefund");
        ResponseEntity<RefundResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req, RefundResponseDto
                .class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> cancelOrder(CancelOrderRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<CancelOrderRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/cancelOrder");

        ResponseEntity<BasicMessage> ret = null;
        try {
            ret = restTemplate.exchange(url, HttpMethod.POST, req, BasicMessage.class);
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return ret;
    }

    @Override
    public FlowTestResult cancelFlow() {
        testCaseIdThreadLocal.set(UUIDUtil.generateUUID());
        List<TestTrace> traces = new ArrayList<>();
        testTracesThreadLocal.set(traces);

        /******************
         * 1st step: login
         *****************/
        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();
        LoginResponseDto loginResponseDto = testLogin(loginRequestDto);
        if (!loginResponseDto.isStatus()) {
            return new FlowTestResult();
        }

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        Map<String, List<String>> headers = loginResponseDto.getHeaders();
        // construct test case info
        TestCase testCase = new TestCase();
        testCase.setUserId(loginRequestDto.getEmail());
        testCase.setSessionId(headers.get(ServiceConstant.COOKIE).toString());
        testCase.setTestCaseId(testCaseIdThreadLocal.get());
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");

        /***************************
         * 2nd step: query tickets
         ***************************/

        OrderQueryRequestDto orderQueryRequestDto = new OrderQueryRequestDto();
        orderQueryRequestDto.disableBoughtDateQuery();
        orderQueryRequestDto.disableStateQuery();
        orderQueryRequestDto.disableTravelDateQuery();
        List<Order> orders = testQueryOrder(headers, orderQueryRequestDto);
        if (orders == null || orders.size() == 0) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
        }

        /*******************************
         * 3rd step: query order other
         ******************************/
        List<Order> orderOthers = testQueryOrderOther(headers, orderQueryRequestDto);
        if (orderOthers == null || orderOthers.size() == 0) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
        }
        /*************************************
         * 4th step: calculate refund
         *************************************/
        if (null != orderOthers && !orderOthers.isEmpty()) {
            orders.addAll(orderOthers);
        }

        // get order ids that status is pay or not pay.
        List<String> orderIds = orders.stream().filter(order -> order.getStatus() == 0 || order.getStatus() == 1)
                .map(order -> order.getId().toString()).collect(Collectors.toList());

        if (!orderIds.isEmpty()) {
            String orderId = RandomUtil.getRandomElementInList(orderIds);
            RefundResponseDto refundResponseDto = testCalculateRefund(headers, orderId);
            if (!refundResponseDto.isStatus()) {
                return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
            }
            /***************************
             * 5th step: cancel order
             ***************************/
            BasicMessage basicMessage = testCancelService(headers, orderId);
            if (!basicMessage.isStatus()) {
                return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
            }
        }
        // construct response ----  passed all flow
        return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
    }


    @Transactional
    public FlowTestResult returnFlowTestResult(List<TestTrace> testTraceList, TestCase testCase) {
        testCaseRepository.save(testCase);
        testTraceRepository.saveAll(testTraceList);
        FlowTestResult bftr = new FlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(testTraceList);
        return bftr;
    }


    private BasicMessage testCancelService(Map<String, List<String>> headers, String orderId) {
        String cancelTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), orderId));

        CancelOrderRequestDto cancelOrderRequestDto = new CancelOrderRequestDto();
        cancelOrderRequestDto.setOrderId(orderId);

        ResponseEntity<BasicMessage> basicMessageResp = cancelOrder(cancelOrderRequestDto, headers);
        BasicMessage basicMessage = basicMessageResp.getBody();

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setEntryApi("/cancelOrder");
        testTrace5.setEntryService("ts-cancel-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        try {
            testTrace5.setReq_param(objectMapper.writeValueAsString(cancelOrderRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace5.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace5.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace5.setTestMethod("cancelOrder");
        testTrace5.setTestTraceId(cancelTraceId);
        testTrace5.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace5.setExpected_result(0);
        testTrace5.setError(0);
        testTrace5.setY_issue_ms("");
        testTrace5.setY_issue_dim_type("");
        testTrace5.setY_issue_dim_content("");
        testTracesThreadLocal.get().add(testTrace5);

        // todo
        // 判断 foodResponseDto 的status 是否为false
        if (basicMessage.getMessage() != null && basicMessage.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.cancelFlowClientAccessTimeMap.get("cancel_Order_flow");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(basicMessage.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);

            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.cancelFlowClientAccessTimeMap.put("cancel_Order_flow", totalAccessTime);
            } else {
                // 此处为引用
                testTrace5 = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace5.setError(1);
                testTrace5.setExpected_result(1);
                testTrace5.setY_issue_ms("ts-cancel-service");
                testTrace5.setY_issue_dim_type("instance");
                testTrace5.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
                basicMessage.setStatus(false);
            }
        }

        return basicMessage;
    }

    private RefundResponseDto testCalculateRefund(Map<String, List<String>> headers, String orderId) {
        String calculateRefundTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), calculateRefundTraceId));

        RefundRequestDto refundRequestDto = new RefundRequestDto();
        refundRequestDto.setOrderId(orderId);

        ResponseEntity<RefundResponseDto> refundResponseDtoResp = calculateRefund(refundRequestDto, headers);
        RefundResponseDto refundResponseDto = refundResponseDtoResp.getBody();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/cancelCalculateRefund");
        testTrace4.setEntryService("ts-cancel-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        try {
            testTrace4.setReq_param(objectMapper.writeValueAsString(refundRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace4.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace4.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace4.setTestMethod("calculateRefund");
        testTrace4.setTestTraceId(calculateRefundTraceId);
        testTrace4.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace4.setExpected_result(0);
        testTrace4.setError(0);
        testTrace4.setY_issue_ms("");
        testTrace4.setY_issue_dim_type("");
        testTrace4.setY_issue_dim_content("");
        testTracesThreadLocal.get().add(testTrace4);

        // todo
        // 判断 foodResponseDto 的status 是否为false
        if (refundResponseDto.getMessage() != null && refundResponseDto.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.cancelFlowClientAccessTimeMap.get("cancel_Calculate_refund");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(refundResponseDto.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);

            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.cancelFlowClientAccessTimeMap.put("cancel_Calculate_refund", totalAccessTime);
            } else {
                // 此处为引用
                testTrace4 = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace4.setError(1);
                testTrace4.setExpected_result(1);
                testTrace4.setY_issue_ms("ts-cancel-service");
                testTrace4.setY_issue_dim_type("instance");
                testTrace4.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
                refundResponseDto.setStatus(false);
            }
        }
        System.out.println(testTrace4);
        return refundResponseDto;
    }

    private List<Order> testQueryOrderOther(Map<String, List<String>> headers, OrderQueryRequestDto
            orderQueryRequestDto) {
        String queryOrderOtherTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), queryOrderOtherTraceId));


        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/orderOther/query");
        testTrace3.setEntryService("ts-order-other-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        try {
            testTrace3.setReq_param(objectMapper.writeValueAsString(orderQueryRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace3.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace3.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace3.setTestMethod("queryOrderOther");
        testTrace3.setTestTraceId(queryOrderOtherTraceId);
        testTrace3.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace3.setExpected_result(0);
        testTrace3.setError(0);
        testTrace3.setY_issue_ms("");
        testTrace3.setY_issue_dim_type("");
        testTrace3.setY_issue_dim_content("");
        testTracesThreadLocal.get().add(testTrace3);


        ResponseEntity<List<Order>> orderOthersResp = queryOrderOther(orderQueryRequestDto, headers);
        List<Order> orderOthers = orderOthersResp.getBody();

        if (orderOthers != null && orderOthers.size() > 0) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.cancelFlowClientAccessTimeMap.get("order_other_query");
            // 服务端返回值
            int totalAccessTime = orderOthers.get(orderOthers.size() - 1).getDocumentType();
            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.cancelFlowClientAccessTimeMap.put("order_other_query", totalAccessTime);
            } else {
                // 此处为引用
                testTrace3 = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace3.setError(1);
                testTrace3.setExpected_result(1);
                testTrace3.setY_issue_ms("ts-order-other-service");
                testTrace3.setY_issue_dim_type("instance");
                testTrace3.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
                orderOthers = null;
            }
        }

        return orderOthers;
    }

    private List<Order> testQueryOrder(Map<String, List<String>> headers, OrderQueryRequestDto orderQueryRequestDto) {
        String queryOrderTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), queryOrderTraceId));

        TestTrace testTrace2 = new TestTrace();
        testTrace2.setEntryApi("/order/query");
        testTrace2.setEntryService("ts-order-service");
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        try {
            testTrace2.setReq_param(objectMapper.writeValueAsString(orderQueryRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace2.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace2.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace2.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace2.setTestMethod("queryOrder");
        testTrace2.setTestTraceId(queryOrderTraceId);
        testTrace2.setExpected_result(0);
        testTrace2.setError(0);
        testTrace2.setY_issue_ms("");
        testTrace2.setY_issue_dim_type("");
        testTrace2.setY_issue_dim_content("");
        testTracesThreadLocal.get().add(testTrace2);
        // orders 为 空数组
        // todo
        ResponseEntity<List<Order>> queryOrderResponseDtosResp = queryOrder(orderQueryRequestDto, headers);
        List<Order> orders = queryOrderResponseDtosResp.getBody();

        if (orders != null && orders.size() > 0) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.cancelFlowClientAccessTimeMap.get("order_query");
            // 服务端返回值
            int totalAccessTime = orders.get(orders.size() - 1).getDocumentType();
            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.cancelFlowClientAccessTimeMap.put("order_query", totalAccessTime);
            } else {
                // 此处为引用
                testTrace2 = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace2.setError(1);
                testTrace2.setExpected_result(1);
                testTrace2.setY_issue_ms("ts-order-service");
                testTrace2.setY_issue_dim_type("instance");
                testTrace2.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
                orders = null;
            }
        }
        return orders;
    }

    private LoginResponseDto testLogin(LoginRequestDto loginRequestDto) {
        String loginTraceId = UUIDUtil.generateUUID();

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.add(ServiceConstant.USER_AGENT, ThreadLocalCache.testCaseIdThreadLocal.get() + "," + loginTraceId);

        loginHeaders.setContentType(MediaType.APPLICATION_JSON);


        TestTrace testTrace = new TestTrace();
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        // TODO
        // loginResponseDto 返回 status false
        testTrace.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(loginTraceId);
        try {
            testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace.setError(0);
        testTrace.setExpected_result(0);
        testTrace.setY_issue_ms("");
        testTrace.setY_issue_dim_type("");
        testTrace.setY_issue_dim_content("");
        testTracesThreadLocal.get().add(testTrace);


        LoginResponseDto loginResponseDto = new LoginResponseDto();
        try {
            ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
            loginResponseDto = loginResponseDtoResp.getBody();
        } catch (Exception e) {
            loginResponseDto.setStatus(false);
        }
        // todo
        //ts-login-service
        if (loginResponseDto.getMessage() != null && loginResponseDto.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.cancelFlowClientAccessTimeMap.get("login");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(loginResponseDto.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.cancelFlowClientAccessTimeMap.put("login", totalAccessTime);
            } else {
                // 此处为引用
                testTrace = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace.setError(1);
                testTrace.setExpected_result(1);
                testTrace.setY_issue_ms("ts-login-service");
                testTrace.setY_issue_dim_type("instance");
                testTrace.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
                loginResponseDto.setStatus(false);
            }
        }
        return loginResponseDto;
    }
}
