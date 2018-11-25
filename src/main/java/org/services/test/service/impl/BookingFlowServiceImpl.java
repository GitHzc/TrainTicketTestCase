package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.service.BookingFlowService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class BookingFlowServiceImpl implements BookingFlowService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private ObjectMapper objectMapper;

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
    public ResponseEntity<List<QueryTicketResponseDto>> queryTicket(QueryTicketRequestDto dto, Map<String,
            List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
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

    @Override
    public ResponseEntity<List<Contact>> getContacts(Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<QueryTicketRequestDto> req = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/contacts/findContacts");
        ResponseEntity<List<Contact>> ret = restTemplate.exchange(url, HttpMethod.GET, req, new
                ParameterizedTypeReference<List<Contact>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<FoodResponseDto> getFood(FoodRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<FoodRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/food/getFood");
        ResponseEntity<FoodResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req, FoodResponseDto.class);

        return ret;
    }

    @Override
    public ResponseEntity<ConfirmResponseDto> preserve(ConfirmRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<ConfirmRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/preserve");
        ResponseEntity<ConfirmResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                ConfirmResponseDto.class);
        return ret;
    }

    @Override
    public ResponseEntity<Boolean> pay(PaymentRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<PaymentRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/inside_payment/pay");
        ResponseEntity<Boolean> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                Boolean.class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> collect(CollectRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<CollectRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/execute/collected");
        ResponseEntity<BasicMessage> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                BasicMessage.class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> enter(ExcuteRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<ExcuteRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/execute/execute");
        ResponseEntity<BasicMessage> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                BasicMessage.class);
        return ret;
    }

    /***************************
     * ticket booking flow test
     ***************************/
    @Override
    public FlowTestResult bookFlow() {

        List<TestTrace> traces = new ArrayList<>();
        testTracesThreadLocal.set(traces);
        testCaseIdThreadLocal.set(UUIDUtil.generateUUID());

        /******************
         * 1st step: login
         *****************/
        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();
        LoginResponseDto loginResponseDto = testLogin(loginRequestDto);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        Map<String, List<String>> headers = loginResponseDto.getHeaders();
        headers.put(ServiceConstant.TEST_CASE_ID, Arrays.asList(testCaseIdThreadLocal.get()));

        // construct test case info
        TestCase testCase = new TestCase();
        testCase.setUserId(loginRequestDto.getEmail());
        testCase.setSessionId(headers.get(ServiceConstant.COOKIE).toString());
        testCase.setTestCaseId(testCaseIdThreadLocal.get());
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");

        /***************************
         * 2nd step: query ticket
         ***************************/
        QueryTicketRequestDto queryTicketRequestDto = ParamUtil.constructQueryTicketReqDto();
        List<QueryTicketResponseDto> queryTicketResponseDtos = testQueryTicket(headers, queryTicketRequestDto);


        /*************************************
         * 3rd step: get contacts
         *************************************/
        List<Contact> contacts = testQueryContact(headers);

        /***********************
         * 4th step: get food
         ***********************/
        // globe field to reuse
        String departureTime = queryTicketRequestDto.getDepartureTime();
        String startingStation = queryTicketRequestDto.getStartingPlace();
        String endingStation = queryTicketRequestDto.getEndPlace();
        String tripId = queryTicketResponseDtos.get(0).getTripId().getType()
                + queryTicketResponseDtos.get(0).getTripId().getNumber(); //默认选第一辆

        FoodRequestDto foodRequestDto = ParamUtil.constructFoodRequestDto(departureTime, startingStation,
                endingStation, tripId);
        testQueryFood(headers, foodRequestDto);

        /******************************
         * 5th step: confirm ticket
         ******************************/
        String contactId = ParamUtil.getRandomContact(contacts);// random param
        ConfirmRequestDto confirmRequestDto = ParamUtil.constructConfirmRequestDto(departureTime, startingStation,
                endingStation, tripId, contactId);
        ConfirmResponseDto confirmResponseDto = testPreserveTicket(headers, confirmRequestDto);

        if (RandomUtil.getRandomTrueOrFalse()) {
            /*********************
             * 6th step: payment
             *********************/
            String orderId = confirmResponseDto.getOrder().getId().toString();
            PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
            testTicketPayment(headers, paymentRequestDto);

            if (RandomUtil.getRandomTrueOrFalse()) {
                /*****************************
                 * 7th step: collect ticket
                 *****************************/
                CollectRequestDto collectRequestDto = ParamUtil.constructCollectRequestDto(orderId);
                testTicketCollection(headers, collectRequestDto);
                if (RandomUtil.getRandomTrueOrFalse()) {
                    /****************************
                     * 8th step: enter station
                     ****************************/
                    ExcuteRequestDto excuteRequestDto = ParamUtil.constructExecuteRequestDto(orderId);
                    testEnterStation(headers, excuteRequestDto);
                }
            }
        }

        // construct response
        FlowTestResult bftr = new FlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(traces);
        return bftr;
    }


    private void testEnterStation(Map<String, List<String>> headers, ExcuteRequestDto excuteRequestDto) {
        String enterTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(enterTraceId));
        ResponseEntity<BasicMessage> enterBasicMsgResp = enter(excuteRequestDto, headers);
        BasicMessage enterBasicMsg = enterBasicMsgResp.getBody();

        TestTrace testTrace8 = new TestTrace();
        testTrace8.setEntryApi("/execute/execute");
        testTrace8.setEntryService("ts-execute-service");
        testTrace8.setEntryTimestamp(System.currentTimeMillis());
        // TODO
        // enterBasicMsg status false

        testTrace8.setError(AssertUtil.assertByStatusCode(enterBasicMsgResp.getStatusCodeValue()));
        testTrace8.setExpected_result(0);
        testTrace8.setSequence(8);
        try {
            testTrace8.setReq_param(objectMapper.writeValueAsString(excuteRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace8.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace8.setTestClass("BookingFlowTestClass");
        testTrace8.setTestMethod("enter");
        testTrace8.setTestTraceId(enterTraceId);
        testTracesThreadLocal.get().add(testTrace8);
        System.out.println(testTrace8);
    }

    private void testTicketCollection(Map<String, List<String>> headers, CollectRequestDto collectRequestDto) {
        String collectTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(collectTraceId));
        ResponseEntity<BasicMessage> collectBasicMsgResp = collect(collectRequestDto, headers);
        BasicMessage collectBasicMsg = collectBasicMsgResp.getBody();

        TestTrace testTrace7 = new TestTrace();
        testTrace7.setEntryApi("/execute/collected");
        testTrace7.setEntryService("ts-execute-service");
        testTrace7.setEntryTimestamp(System.currentTimeMillis());
        // TODO
        // collectBasicMsg  status false
        testTrace7.setError(AssertUtil.assertByStatusCode(collectBasicMsgResp.getStatusCodeValue()));
        testTrace7.setExpected_result(0);
        testTrace7.setSequence(7);
        try {
            testTrace7.setReq_param(objectMapper.writeValueAsString(collectRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace7.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace7.setTestClass("BookingFlowTestClass");
        testTrace7.setTestMethod("collect");
        testTrace7.setTestTraceId(collectTraceId);
        testTracesThreadLocal.get().add(testTrace7);
        System.out.println(testTrace7);
    }

    private void testTicketPayment(Map<String, List<String>> headers, PaymentRequestDto paymentRequestDto) {
        String paymentTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(paymentTraceId));
        ResponseEntity<Boolean> paymentStatusResp = pay(paymentRequestDto, headers);
        boolean paymentStatus = paymentStatusResp.getBody();

        TestTrace testTrace6 = new TestTrace();
        testTrace6.setEntryApi("/inside_payment/pay");
        testTrace6.setEntryService("ts-inside-payment-service");
        testTrace6.setEntryTimestamp(System.currentTimeMillis());
        // todo
        // paymentStatus   是 false

        testTrace6.setError(AssertUtil.assertByStatusCode(paymentStatusResp.getStatusCodeValue()));
        testTrace6.setExpected_result(0);
        testTrace6.setSequence(6);
        try {
            testTrace6.setReq_param(objectMapper.writeValueAsString(paymentRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace6.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace6.setTestClass("BookingFlowTestClass");
        testTrace6.setTestMethod("pay");
        testTrace6.setTestTraceId(paymentTraceId);
        testTracesThreadLocal.get().add(testTrace6);
        System.out.println(testTrace6);
    }

    private ConfirmResponseDto testPreserveTicket(Map<String, List<String>> headers, ConfirmRequestDto
            confirmRequestDto) {
        String confirmTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(confirmTraceId));
        ResponseEntity<ConfirmResponseDto> confirmResponseDtoResp = preserve(confirmRequestDto, headers);
        ConfirmResponseDto confirmResponseDto = confirmResponseDtoResp.getBody();

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setEntryApi("/preserve");
        testTrace5.setEntryService("ts-preserve-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        testTrace5.setExpected_result(0);
        testTrace5.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace5.setTestClass("BookingFlowTestClass");
        testTrace5.setTestMethod("preserve");
        testTrace5.setTestTraceId(confirmTraceId);
        testTrace5.setSequence(5);
        try {
            testTrace5.setReq_param(objectMapper.writeValueAsString(confirmRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (AssertUtil.assertByStatusCode(confirmResponseDtoResp.getStatusCodeValue()) == 1) {
            testTrace5.setError(1);
            testTrace5.setY_issue_dim_type("seq");
            testTrace5.setY_issue_dim_content("");
            testTrace5.setY_issue_ms("ts-preserve-service");
        }

        testTracesThreadLocal.get().add(testTrace5);
        return confirmResponseDto;
    }

    private void testQueryFood(Map<String, List<String>> headers, FoodRequestDto foodRequestDto) {
        String foodTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(foodTraceId));
        ResponseEntity<FoodResponseDto> foodResponseDtoResp = getFood(foodRequestDto, headers);
        FoodResponseDto foodResponseDto = foodResponseDtoResp.getBody();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/food/getFood");
        testTrace4.setEntryService("ts-food-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        // todo
        // 判断 foodResponseDto 的status 是否为false
        testTrace4.setError(AssertUtil.assertByStatusCode(foodResponseDtoResp.getStatusCodeValue()));
        testTrace4.setExpected_result(0);
        try {
            testTrace4.setReq_param(objectMapper.writeValueAsString(foodRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace4.setSequence(4);
        testTrace4.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace4.setTestClass("BookingFlowTestClass");
        testTrace4.setTestMethod("getFood");
        testTrace4.setTestTraceId(foodTraceId);
        testTracesThreadLocal.get().add(testTrace4);
        System.out.println(testTrace4);
    }

    private List<Contact> testQueryContact(Map<String, List<String>> headers) {
        String contactTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(contactTraceId));
        ResponseEntity<List<Contact>> contactsResp = getContacts(headers);
        List<Contact> contacts = contactsResp.getBody();

        if (null == contacts || contacts.isEmpty() || AssertUtil.assertByStatusCode(contactsResp.getStatusCodeValue()
        ) == 1) {
            throw new RuntimeException("query contact error");
        }

        TestTrace testTrace3 = new TestTrace();
        testTrace3.setError(0);
        testTrace3.setEntryApi("/contacts/findContacts");
        testTrace3.setEntryService("ts-contacts-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        testTrace3.setExpected_result(0);
        testTrace3.setSequence(3);
        try {
            testTrace3.setReq_param(objectMapper.writeValueAsString(null));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace3.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace3.setTestClass("BookingFlowTestClass");
        testTrace3.setTestMethod("getContacts");
        testTrace3.setTestTraceId(contactTraceId);
        testTracesThreadLocal.get().add(testTrace3);
        System.out.println(testTrace3);
        return contacts;
    }

    private List<QueryTicketResponseDto> testQueryTicket(Map<String, List<String>> headers, QueryTicketRequestDto
            queryTicketRequestDto) {
        String queryTicketTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.TEST_TRACE_ID, Arrays.asList(queryTicketTraceId));
        ResponseEntity<List<QueryTicketResponseDto>> queryTicketResponseDtosResp = queryTicket(queryTicketRequestDto,
                headers);
        List<QueryTicketResponseDto> queryTicketResponseDtos = queryTicketResponseDtosResp.getBody();
        if (null == queryTicketResponseDtos || queryTicketResponseDtos.isEmpty() ||
                AssertUtil.assertByStatusCode(queryTicketResponseDtosResp.getStatusCodeValue()) == 1) {
            throw new RuntimeException("No ticket found");
        }

        TestTrace testTrace2 = new TestTrace();
        if (AssertUtil.assertByStatusCode(queryTicketResponseDtosResp.getStatusCodeValue()) == 1) {
            testTrace2.setError(1);
            throw new RuntimeException("query ticket error");
        } else {
            testTrace2.setError(0);
        }
        if (queryTicketRequestDto.getEndPlace().equals(ServiceConstant.NAN_JING)) {
            testTrace2.setEntryApi("/travel2/query");
            testTrace2.setEntryService("ts-travel2-service");
        } else {
            testTrace2.setEntryApi("/travel/query");
            testTrace2.setEntryService("ts-travel-service");
        }
        testTrace2.setSequence(2);
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setExpected_result(0);
        try {
            testTrace2.setReq_param(objectMapper.writeValueAsString(queryTicketRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace2.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace2.setTestClass("BookingFlowTestClass");
        testTrace2.setTestMethod("queryTicket");
        testTrace2.setTestTraceId(queryTicketTraceId);

        testTracesThreadLocal.get().add(testTrace2);
        return queryTicketResponseDtos;
    }

    private LoginResponseDto testLogin(LoginRequestDto loginRequestDto) {
        String loginTraceId = UUIDUtil.generateUUID();

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.add(ServiceConstant.TEST_CASE_ID, testCaseIdThreadLocal.get());
        loginHeaders.add(ServiceConstant.TEST_TRACE_ID, loginTraceId);
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
        LoginResponseDto loginResponseDto = loginResponseDtoResp.getBody();

        if (null == loginResponseDto || AssertUtil.assertByStatusCode(loginResponseDtoResp.getStatusCodeValue()) == 1 ||
                !loginResponseDto.isStatus()) {
            throw new RuntimeException("login error");
        }

        TestTrace testTrace = new TestTrace();
        testTrace.setError(0);
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setSequence(1);
        testTrace.setExpected_result(0);
        testTrace.setTestCaseId(testCaseIdThreadLocal.get());
        testTrace.setTestClass("BookingFlowTestClass");
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(loginTraceId);
        try {
            testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        testTracesThreadLocal.get().add(testTrace);
        return loginResponseDto;
    }
}
