package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.service.CancelFlowService;
import org.services.test.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
    private BookingFlowServiceImpl bookingFlowServiceImpl;

    private static final Logger logger = LoggerFactory.getLogger(CancelFlowServiceImpl.class);

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
        ResponseEntity<BasicMessage> ret = restTemplate.exchange(url, HttpMethod.POST, req, BasicMessage.class);
        return ret;
    }

    @Override
    public FlowTestResult cancelFlow() throws JsonProcessingException {

        FlowTestResult flowTestResult = new FlowTestResult();

        /******************
         * 1st step: login
         *****************/
        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();

        LoginResponseDto loginResponseDto = testLogin(loginRequestDto);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        Map<String, List<String>> headers = loginResponseDto.getHeaders();

        /***************************
         * 2nd step: query tickets
         ***************************/

        OrderQueryRequestDto orderQueryRequestDto = new OrderQueryRequestDto();
        orderQueryRequestDto.disableBoughtDateQuery();
        orderQueryRequestDto.disableStateQuery();
        orderQueryRequestDto.disableTravelDateQuery();
        List<Order> orders = testQueryOrder(headers, orderQueryRequestDto);
        /*******************************
         * 3rd step: query order other
         ******************************/
        List<Order> orderOthers = testQueryOrderOther(headers, orderQueryRequestDto);

        /*************************************
         * 4th step: calculate refund
         *************************************/

        // get order ids that status is pay or not pay.
        List<String> orderIds = orders.stream().filter(order -> order.getStatus() == 0 || order.getStatus() == 1)
                .map(order -> order.getId().toString()).collect(Collectors.toList());
        List<String> orderOtherIds = orderOthers.stream().filter(order -> order.getStatus() == 0 || order.getStatus()
                == 1).map(order -> order.getId().toString()).collect(Collectors.toList());

        String orderId = null;
        if (RandomUtil.getRandomTrueOrFalse()){
            if (!orderIds.isEmpty()) {
                orderId = RandomUtil.getRandomElementInList(orderIds);
                ThreadLocalCache.cancelOrderType.set("cancelOrder");
            }
        }else {
            if (!orderOtherIds.isEmpty()) {
                orderId = RandomUtil.getRandomElementInList(orderOtherIds);
                ThreadLocalCache.cancelOrderType.set("cancelOrderOther");
            }
        }

        if (null != orderId) {

            testCalculateRefund(headers, orderId);

            /***************************
             * 5th step: cancel order
             ***************************/
            testCancelService(headers, orderId);
        }

        return flowTestResult;
    }

    private BasicMessage testCancelService(Map<String, List<String>> headers, String orderId) throws
            JsonProcessingException {
        String cancelTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                cancelTraceId));
        CancelOrderRequestDto cancelOrderRequestDto = new CancelOrderRequestDto();
        cancelOrderRequestDto.setOrderId(orderId);

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace5.setEntryApi("/cancelOrder");
        testTrace5.setEntryService("ts-cancel-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        testTrace5.setError(0);
        testTrace5.setExpected_result(0);
        testTrace5.setReq_param(objectMapper.writeValueAsString(cancelOrderRequestDto));
        testTrace5.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace5.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace5.setTestMethod("cancelOrder");
        testTrace5.setTestTraceId(cancelTraceId);
        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace5);
        logger.info(testTrace5.toString());

        ResponseEntity<BasicMessage> basicMessageResp = cancelOrder(cancelOrderRequestDto, headers);
        BasicMessage basicMessage = basicMessageResp.getBody();

        return basicMessage;
    }

    private RefundResponseDto testCalculateRefund(Map<String, List<String>> headers, String orderId) throws
            JsonProcessingException {
        RefundRequestDto refundRequestDto = new RefundRequestDto();
        refundRequestDto.setOrderId(orderId);

        ResponseEntity<RefundResponseDto> refundResponseDtoResp = calculateRefund(refundRequestDto, headers);
        RefundResponseDto refundResponseDto = refundResponseDtoResp.getBody();

        return refundResponseDto;
    }

    private List<Order> testQueryOrderOther(Map<String, List<String>> headers, OrderQueryRequestDto
            orderQueryRequestDto) throws JsonProcessingException {
        ResponseEntity<List<Order>> orderOthersResp = queryOrderOther(orderQueryRequestDto, headers);
        List<Order> orderOthers = orderOthersResp.getBody();

        return orderOthers;
    }

    private List<Order> testQueryOrder(Map<String, List<String>> headers, OrderQueryRequestDto orderQueryRequestDto)
            throws JsonProcessingException {
        ResponseEntity<List<Order>> queryOrderResponseDtosResp = queryOrder(orderQueryRequestDto, headers);
        List<Order> orders = queryOrderResponseDtosResp.getBody();

        return orders;
    }

    private LoginResponseDto testLogin(LoginRequestDto loginRequestDto) throws JsonProcessingException {
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
        LoginResponseDto loginResponseDto = loginResponseDtoResp.getBody();

        return loginResponseDto;
    }
}