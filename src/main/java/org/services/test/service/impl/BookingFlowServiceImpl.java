package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.exception.ConfigFaultException;
import org.services.test.exception.UnknownException;
import org.services.test.repository.TestCaseRepository;
import org.services.test.repository.TestTraceRepository;
import org.services.test.service.BookingFlowService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
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

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private TestTraceRepository testTraceRepository;


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
        ResponseEntity<List<QueryTicketResponseDto>> ret = null;
        try {
            ret =restTemplate.exchange(url, HttpMethod.POST, req,
                    new ParameterizedTypeReference<List<QueryTicketResponseDto>>() {
                    });
        } catch (Exception e) {
            if (e instanceof ResourceAccessException) {
                throw new ConfigFaultException("memory error");
            }
            else if (e instanceof ConfigFaultException || e instanceof UnknownException) {
                throw e;
            }
        }

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

        String uri;
        if (dto.getTo().equals(ServiceConstant.NAN_JING)) {
            uri = "/preserveOther";
        } else {
            uri = "/preserve";
        }

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), uri);
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
    public FlowTestResult bookFlow() throws Exception {
        List<TestTrace> traces = new ArrayList<>();
        ThreadLocalCache.testTracesThreadLocal.set(traces);
        ThreadLocalCache.testCaseIdThreadLocal.set(UUIDUtil.generateUUID());

        FlowTestResult flowTestResult = new FlowTestResult();

        /******************
         * 1st step: login
         *****************/
        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();

        // construct test case info
        ThreadLocalCache.testCaseThreadLocal.set(constructTestCase(loginRequestDto, new HashMap<>(), ThreadLocalCache.testCaseIdThreadLocal.get()));

        LoginResponseDto loginResponseDto = testLogin(loginRequestDto);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        Map<String, List<String>> headers = loginResponseDto.getHeaders();
        headers.put(ServiceConstant.TEST_CASE_ID, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get()));

        // set cookie in test case
        ThreadLocalCache.testCaseThreadLocal.get().setSessionId(String.valueOf(headers.get(ServiceConstant.COOKIE)));




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
        if (null == confirmResponseDto || null == confirmResponseDto.getOrder()) {
            flowTestResult.setTestCase(ThreadLocalCache.testCaseThreadLocal.get());
            flowTestResult.setTestTraces(ThreadLocalCache.testTracesThreadLocal.get());
            persistTestData(ThreadLocalCache.testCaseThreadLocal.get(), ThreadLocalCache.testTracesThreadLocal.get());
            return flowTestResult;
        }


        // get random number in [0, 4)
        int randomNumber = new Random().nextInt(4);
        // int randomNumber = 0;
        System.out.println("===================randomNumber: " + randomNumber);

        switch (randomNumber) {
            case 0: { // don't execute step 6, 7 and 8
                break;
            }
            case 1: { // execute step 6
                /*********************
                 * 6th step: payment
                 *********************/
                String orderId = confirmResponseDto.getOrder().getId().toString();
                PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
                testTicketPayment(headers, paymentRequestDto);
                break;
            }
            case 2: { // execute step 6 and step 7
                /*********************
                 * 6th step: payment
                 *********************/
                String orderId = confirmResponseDto.getOrder().getId().toString();
                PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
                testTicketPayment(headers, paymentRequestDto);

                /*****************************
                 * 7th step: collect ticket
                 *****************************/
                CollectRequestDto collectRequestDto = ParamUtil.constructCollectRequestDto(orderId);
                testTicketCollection(headers, collectRequestDto);
                break;
            }
            case 3: { // execute step 6, 7 and 8
                /*********************
                 * 6th step: payment
                 *********************/
                String orderId = confirmResponseDto.getOrder().getId().toString();
                PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
                testTicketPayment(headers, paymentRequestDto);

                /*****************************
                 * 7th step: collect ticket
                 *****************************/
                CollectRequestDto collectRequestDto = ParamUtil.constructCollectRequestDto(orderId);
                testTicketCollection(headers, collectRequestDto);

                /****************************
                 * 8th step: enter station
                 ****************************/
                ExcuteRequestDto excuteRequestDto = ParamUtil.constructExecuteRequestDto(orderId);
                testEnterStation(headers, excuteRequestDto);
                break;
            }
            default:
                break;
        }

        flowTestResult.setTestCase(ThreadLocalCache.testCaseThreadLocal.get());
        flowTestResult.setTestTraces(ThreadLocalCache.testTracesThreadLocal.get());
        persistTestData(ThreadLocalCache.testCaseThreadLocal.get(), ThreadLocalCache.testTracesThreadLocal.get());
        return flowTestResult;
    }


    private void testEnterStation(Map<String, List<String>> headers, ExcuteRequestDto excuteRequestDto) {
        String enterTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace8 = new TestTrace();
        testTrace8.setEntryApi("/execute/execute");
        testTrace8.setEntryService("ts-execute-service");
        testTrace8.setEntryTimestamp(System.currentTimeMillis());

        testTrace8.setExpected_result(0);
        try {
            testTrace8.setReq_param(objectMapper.writeValueAsString(excuteRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace8.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace8.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace8.setTestMethod("enter");
        testTrace8.setTestTraceId(enterTraceId);
        testTrace8.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace8);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), enterTraceId));
        ResponseEntity<BasicMessage> enterBasicMsgResp = enter(excuteRequestDto, headers);
        BasicMessage enterBasicMsg = enterBasicMsgResp.getBody();
    }

    private void testTicketCollection(Map<String, List<String>> headers, CollectRequestDto collectRequestDto) {
        String collectTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace7 = new TestTrace();
        testTrace7.setEntryApi("/execute/collected");
        testTrace7.setEntryService("ts-execute-service");
        testTrace7.setEntryTimestamp(System.currentTimeMillis());

        try {
            testTrace7.setReq_param(objectMapper.writeValueAsString(collectRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace7.setExpected_result(0);
        testTrace7.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace7.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace7.setTestMethod("collect");
        testTrace7.setTestTraceId(collectTraceId);
        testTrace7.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace7);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), collectTraceId));
        ResponseEntity<BasicMessage> collectBasicMsgResp = collect(collectRequestDto, headers);
        BasicMessage collectBasicMsg = collectBasicMsgResp.getBody();
    }

    private void testTicketPayment(Map<String, List<String>> headers, PaymentRequestDto paymentRequestDto) {
        String paymentTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace6 = new TestTrace();
        testTrace6.setEntryApi("/inside_payment/pay");
        testTrace6.setEntryService("ts-inside-payment-service");
        testTrace6.setEntryTimestamp(System.currentTimeMillis());
        testTrace6.setExpected_result(0);
        try {
            testTrace6.setReq_param(objectMapper.writeValueAsString(paymentRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace6.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace6.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace6.setTestMethod("pay");
        testTrace6.setTestTraceId(paymentTraceId);
        testTrace6.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace6);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), paymentTraceId));
        pay(paymentRequestDto, headers);
    }

    private ConfirmResponseDto testPreserveTicket(Map<String, List<String>> headers, ConfirmRequestDto
            confirmRequestDto) {
        String confirmTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace5 = new TestTrace();
        if (!confirmRequestDto.getTo().equals(ServiceConstant.NAN_JING)) {
            testTrace5.setEntryApi("/preserve");
            testTrace5.setEntryService("ts-preserve-service");
        }
        else {
            testTrace5.setEntryApi("/preserveOther");
            testTrace5.setEntryService("ts-preserve-other-service");
        }

        testTrace5.setEntryTimestamp(System.currentTimeMillis());

        try {
            testTrace5.setReq_param(objectMapper.writeValueAsString(confirmRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace5.setExpected_result(0);
        testTrace5.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace5.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace5.setTestMethod("preserve");
        testTrace5.setTestTraceId(confirmTraceId);
        testTrace5.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace5);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), confirmTraceId));
        ResponseEntity<ConfirmResponseDto> confirmResponseDtoResp = preserve(confirmRequestDto, headers);

        return confirmResponseDtoResp.getBody();
    }

    private void testQueryFood(Map<String, List<String>> headers, FoodRequestDto foodRequestDto) {
        String foodTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/food/getFood");
        testTrace4.setEntryService("ts-food-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());

        try {
            testTrace4.setReq_param(objectMapper.writeValueAsString(foodRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        testTrace4.setExpected_result(0);
        testTrace4.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace4.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace4.setTestMethod("getFood");
        testTrace4.setTestTraceId(foodTraceId);
        testTrace4.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace4);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), foodTraceId));
        getFood(foodRequestDto, headers);
    }

    private List<Contact> testQueryContact(Map<String, List<String>> headers) throws Exception {
        String contactTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/contacts/findContacts");
        testTrace3.setEntryService("ts-contacts-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());

        try {
            testTrace3.setReq_param(objectMapper.writeValueAsString(null));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        testTrace3.setExpected_result(0);
        testTrace3.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace3.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace3.setTestMethod("getContacts");
        testTrace3.setTestTraceId(contactTraceId);
        testTrace3.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace3);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), contactTraceId));
        ResponseEntity<List<Contact>> contactsResp = getContacts(headers);

        return contactsResp.getBody();
    }

    private List<QueryTicketResponseDto> testQueryTicket(Map<String, List<String>> headers, QueryTicketRequestDto
            queryTicketRequestDto) {
        String queryTicketTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace2 = new TestTrace();
        if (queryTicketRequestDto.getEndPlace().equals(ServiceConstant.NAN_JING)){
            testTrace2.setEntryApi("/travel2/query");
            testTrace2.setEntryService("ts-travel2-service");
        } else {
            testTrace2.setEntryApi("/travel/query");
            testTrace2.setEntryService("ts-travel-service");
        }

        try {
            testTrace2.setReq_param(objectMapper.writeValueAsString(queryTicketRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setExpected_result(0);
        testTrace2.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace2.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace2.setTestMethod("queryTicket");
        testTrace2.setTestTraceId(queryTicketTraceId);
        testTrace2.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace2);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), queryTicketTraceId));
        ResponseEntity<List<QueryTicketResponseDto>> queryTicketResponseDtosResp = queryTicket(queryTicketRequestDto, headers);

        return queryTicketResponseDtosResp.getBody();
    }

    protected LoginResponseDto testLogin(LoginRequestDto loginRequestDto) throws Exception {
        String loginTraceId = UUIDUtil.generateUUID();

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.add(ServiceConstant.USER_AGENT, ThreadLocalCache.testCaseIdThreadLocal.get());
        loginHeaders.add(ServiceConstant.USER_AGENT, loginTraceId);
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        TestTrace testTrace = new TestTrace();
        testTrace.setTestTraceId(loginTraceId);
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setExpected_result(0); // need to check
        testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        testTrace.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace.setTestClass(ServiceConstant.COMMON_SERVICE);
        testTrace.setTestMethod("login");
        testTrace.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace);

        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);

        return loginResponseDtoResp.getBody();
    }


    protected TestCase constructTestCase(LoginRequestDto loginRequestDto, Map<String, List<String>> headers, String testCaseId) {
        TestCase testCase = new TestCase();
        testCase.setUserId(loginRequestDto.getEmail());
        testCase.setSessionId(String.valueOf(headers.get(ServiceConstant.COOKIE)));
        testCase.setTestCaseId(testCaseId);
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");
        return testCase;
    }

    @Transactional
    public void persistTestData(TestCase testCase, List<TestTrace> testTraces) {
        testCaseRepository.save(testCase);
        testTraceRepository.saveAll(testTraces);
    }
}
