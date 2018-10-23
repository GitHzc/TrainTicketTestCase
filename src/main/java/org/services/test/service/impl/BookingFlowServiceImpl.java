package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.dto.*;
import org.services.test.service.BookingFlowService;
import org.services.test.util.AssertUtil;
import org.services.test.util.UUIDUtil;
import org.services.test.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingFlowServiceImpl implements BookingFlowService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpHeaders httpHeaders) {
        HttpEntity<LoginRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/login");
        ResponseEntity<LoginResponseDto> resp = restTemplate.exchange(url, HttpMethod.POST, req,
                LoginResponseDto.class);

        HttpHeaders responseHeaders = resp.getHeaders();
        List<String> values = responseHeaders.get("set-Cookie");
        List<String> respCookieValue = new ArrayList<>();
        for (String cookie : values) {
            respCookieValue.add(cookie.split(";")[0]);
        }
        LoginResponseDto ret = resp.getBody();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Cookie", respCookieValue);
        ret.setHeaders(headers);
        return resp;
    }

    @Override
    public ResponseEntity<List<QueryTicketResponseDto>> queryTicket(QueryTicketRequestDto dto, Map<String,
            List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
        HttpEntity<QueryTicketRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/travel/query");
        ResponseEntity<List<QueryTicketResponseDto>> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                new ParameterizedTypeReference<List<QueryTicketResponseDto>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<List<Contact>> getContacts(Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
        HttpEntity<QueryTicketRequestDto> req = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/contacts/findContacts");
        ResponseEntity<List<Contact>> ret = restTemplate.exchange(url, HttpMethod.GET, req, new
                ParameterizedTypeReference<List<Contact>>() {
                });
        return ret;
    }

    @Override
    public ResponseEntity<FoodResponseDto> getFood(FoodRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
        HttpEntity<FoodRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/food/getFood");
        ResponseEntity<FoodResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req, FoodResponseDto.class);

        return ret;
    }

    @Override
    public ResponseEntity<ConfirmResponseDto> preserve(ConfirmRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
        HttpEntity<ConfirmRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/preserve");
        ResponseEntity<ConfirmResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                ConfirmResponseDto.class);
        return ret;
    }

    @Override
    public ResponseEntity<Boolean> pay(PaymentRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
        HttpEntity<PaymentRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/inside_payment/pay");
        ResponseEntity<Boolean> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                Boolean.class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> collect(CollectRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
        HttpEntity<CollectRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/execute/collected");
        ResponseEntity<BasicMessage> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                BasicMessage.class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> enter(ExcuteRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = setHeader(headers);
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
    public BookingFlowTestResult bookFlow() {

        String testCaseID = UUIDUtil.generateUUID();

        /******************
         * 1st step: login
         *****************/
        LoginRequestDto loginRequestDto = constructLoginRequestDto();

        HttpHeaders LoginHeaders = new HttpHeaders();
        LoginHeaders.add("Cookie", "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        LoginHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, LoginHeaders);
        LoginResponseDto loginResponseDto = loginResponseDtoResp.getBody();

        TestTrace testTrace = new TestTrace();
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setError(AssertUtil.assertByStatusCode(loginResponseDtoResp.getStatusCodeValue()));
        testTrace.setExpected_result(0);
        try {
            testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace.setTestCaseId(testCaseID);
        testTrace.setTestClass("BookingFlowTestClass");
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        Map<String, List<String>> headers = loginResponseDto.getHeaders();

        // construct test case info
        TestCase testCase = new TestCase();
        testCase.setUserId(loginRequestDto.getEmail());
        testCase.setSessionId(headers.get("Cookie").toString());
        testCase.setTestCaseId(testCaseID);
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");

        /***************************
         * 2nd step: query ticket
         ***************************/
        QueryTicketRequestDto queryTicketRequestDto = constructQueryTicketReqDto();
        ResponseEntity<List<QueryTicketResponseDto>> queryTicketResponseDtosResp = queryTicket(queryTicketRequestDto,
                headers);
        List<QueryTicketResponseDto> queryTicketResponseDtos = queryTicketResponseDtosResp.getBody();

        TestTrace testTrace2 = new TestTrace();
        testTrace2.setEntryApi("/travel/query");
        testTrace2.setEntryService("ts-travel-service");
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setError(AssertUtil.assertByStatusCode(queryTicketResponseDtosResp.getStatusCodeValue()));
        testTrace2.setExpected_result(0);
        try {
            testTrace2.setReq_param(objectMapper.writeValueAsString(queryTicketResponseDtos));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace2.setTestCaseId(testCaseID);
        testTrace2.setTestClass("BookingFlowTestClass");
        testTrace2.setTestMethod("queryTicket");
        testTrace2.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace2);

        /*************************************
         * 3rd step: get contacts
         *************************************/
        ResponseEntity<List<Contact>> contactsResp = getContacts(headers);
        List<Contact> contacts = contactsResp.getBody();

        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/contacts/findContacts");
        testTrace3.setEntryService("ts-contacts-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        testTrace3.setError(AssertUtil.assertByStatusCode(contactsResp.getStatusCodeValue()));
        testTrace3.setExpected_result(0);
        try {
            testTrace3.setReq_param(objectMapper.writeValueAsString(null));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace3.setTestCaseId(testCaseID);
        testTrace3.setTestClass("BookingFlowTestClass");
        testTrace3.setTestMethod("getContacts");
        testTrace3.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace3);

        /***********************
         * 4th step: get food
         ***********************/
        // globe field to reuse
        String departureTime = queryTicketRequestDto.getDepartureTime();
        String startingStation = queryTicketRequestDto.getStartingPlace();
        String endingStation = queryTicketRequestDto.getEndPlace();
        String tripId = queryTicketResponseDtos.get(0).getTripId().getType()
                + queryTicketResponseDtos.get(0).getTripId().getNumber(); //默认选第一辆

        FoodRequestDto foodRequestDto = new FoodRequestDto();
        foodRequestDto.setDate(departureTime);
        foodRequestDto.setStartStation(startingStation);
        foodRequestDto.setEndStation(endingStation);
        foodRequestDto.setTripId(tripId);
        ResponseEntity<FoodResponseDto> foodResponseDtoResp = getFood(foodRequestDto, headers);
        FoodResponseDto foodResponseDto = foodResponseDtoResp.getBody();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/food/getFood");
        testTrace4.setEntryService("ts-food-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        testTrace4.setError(AssertUtil.assertByStatusCode(foodResponseDtoResp.getStatusCodeValue()));
        testTrace4.setExpected_result(0);
        try {
            testTrace4.setReq_param(objectMapper.writeValueAsString(foodRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace4.setTestCaseId(testCaseID);
        testTrace4.setTestClass("BookingFlowTestClass");
        testTrace4.setTestMethod("getFood");
        testTrace4.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace4);

        /******************************
         * 5th step: confirm ticket
         ******************************/
        String contactId = contacts.get(0).getId();// 默认取第一个联系人
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        ConfirmRequestDto confirmRequestDto = new ConfirmRequestDto();
        confirmRequestDto.setContactsId(contactId);
        confirmRequestDto.setTripId(tripId);
        confirmRequestDto.setSeatType(2); // seat type 2, firstClassSeat
        try {
            confirmRequestDto.setDate(simpleDateFormat.parse(departureTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        confirmRequestDto.setFrom(startingStation);
        confirmRequestDto.setTo(endingStation);
        confirmRequestDto.setAssurance(0); // 不选保险
        confirmRequestDto.setFoodType(0); // 不选吃的

        ResponseEntity<ConfirmResponseDto> confirmResponseDtoResp = preserve(confirmRequestDto, headers);
        ConfirmResponseDto confirmResponseDto = confirmResponseDtoResp.getBody();

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setEntryApi("/preserve");
        testTrace5.setEntryService("ts-preserve-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        testTrace5.setError(AssertUtil.assertByStatusCode(confirmResponseDtoResp.getStatusCodeValue()));
        testTrace5.setExpected_result(0);
        try {
            testTrace5.setReq_param(objectMapper.writeValueAsString(confirmResponseDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace5.setTestCaseId(testCaseID);
        testTrace5.setTestClass("BookingFlowTestClass");
        testTrace5.setTestMethod("preserve");
        testTrace5.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace5);

        /*********************
         * 6th step: payment
         *********************/
        String orderId = confirmResponseDto.getOrder().getId().toString();
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto();
        paymentRequestDto.setOrderId(orderId);
        paymentRequestDto.setTripId(tripId);

        ResponseEntity<Boolean> paymentStatusResp = pay(paymentRequestDto, headers);
        boolean paymentStatus = paymentStatusResp.getBody();

        TestTrace testTrace6 = new TestTrace();
        testTrace6.setEntryApi("/inside_payment/pay");
        testTrace6.setEntryService("ts-inside-payment-service");
        testTrace6.setEntryTimestamp(System.currentTimeMillis());
        testTrace6.setError(AssertUtil.assertByStatusCode(paymentStatusResp.getStatusCodeValue()));
        testTrace6.setExpected_result(0);
        try {
            testTrace6.setReq_param(objectMapper.writeValueAsString(paymentRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace6.setTestCaseId(testCaseID);
        testTrace6.setTestClass("BookingFlowTestClass");
        testTrace6.setTestMethod("pay");
        testTrace6.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace6);

        /*****************************
         * 7th step: collect ticket
         *****************************/
        CollectRequestDto collectRequestDto = new CollectRequestDto();
        collectRequestDto.setOrderId(orderId);
        ResponseEntity<BasicMessage> collectBasicMsgResp = collect(collectRequestDto, headers);
        BasicMessage collectBasicMsg = collectBasicMsgResp.getBody();

        TestTrace testTrace7 = new TestTrace();
        testTrace7.setEntryApi("/execute/collected");
        testTrace7.setEntryService("ts-execute-service");
        testTrace7.setEntryTimestamp(System.currentTimeMillis());
        testTrace7.setError(AssertUtil.assertByStatusCode(collectBasicMsgResp.getStatusCodeValue()));
        testTrace7.setExpected_result(0);
        try {
            testTrace7.setReq_param(objectMapper.writeValueAsString(collectRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace7.setTestCaseId(testCaseID);
        testTrace7.setTestClass("BookingFlowTestClass");
        testTrace7.setTestMethod("collect");
        testTrace7.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace7);

        /****************************
         * 8th step: enter station
         ****************************/
        ExcuteRequestDto excuteRequestDto = new ExcuteRequestDto();
        excuteRequestDto.setOrderId(orderId);
        ResponseEntity<BasicMessage> enterBasicMsgResp = enter(excuteRequestDto, headers);
        BasicMessage enterBasicMsg = enterBasicMsgResp.getBody();

        TestTrace testTrace8 = new TestTrace();
        testTrace8.setEntryApi("/execute/execute");
        testTrace8.setEntryService("ts-execute-service");
        testTrace8.setEntryTimestamp(System.currentTimeMillis());
        testTrace8.setError(AssertUtil.assertByStatusCode(enterBasicMsgResp.getStatusCodeValue()));
        testTrace8.setExpected_result(0);
        try {
            testTrace8.setReq_param(objectMapper.writeValueAsString(excuteRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace8.setTestCaseId(testCaseID);
        testTrace8.setTestClass("BookingFlowTestClass");
        testTrace8.setTestMethod("enter");
        testTrace8.setTestTraceId(UUIDUtil.generateUUID());
        System.out.println(testTrace8);

        // construct response
        List<TestTrace> traces = new ArrayList<>();
        traces.add(testTrace);
        traces.add(testTrace2);
        traces.add(testTrace3);
        traces.add(testTrace4);
        traces.add(testTrace5);
        traces.add(testTrace6);
        traces.add(testTrace7);
        traces.add(testTrace8);


        BookingFlowTestResult bftr = new BookingFlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(traces);
        return bftr;
    }

    private HttpHeaders setHeader(Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        for (Map.Entry entry : headers.entrySet()) {
            List<String> values = (List<String>) entry.getValue();
            for (String value : values) {
                httpHeaders.add("Cookie", value);
            }
        }

        return httpHeaders;
    }

    /**********************************************
     * generate random parameter for test method
     **********************************************/
    private LoginRequestDto constructLoginRequestDto() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("fdse_microservices@163.com");
        loginRequestDto.setPassword("DefaultPassword");
        loginRequestDto.setVerificationCode("abcd");
        return loginRequestDto;
    }

    private QueryTicketRequestDto constructQueryTicketReqDto() {
        QueryTicketRequestDto queryTicketRequestDto = new QueryTicketRequestDto();
        queryTicketRequestDto.setStartingPlace("Shang Hai");
        queryTicketRequestDto.setEndPlace("Su Zhou");
        queryTicketRequestDto.setDepartureTime("2018-10-28");
        return queryTicketRequestDto;
    }
}
