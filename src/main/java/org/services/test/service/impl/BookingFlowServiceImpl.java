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
import org.services.test.service.BookingFlowService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BookingFlowServiceImpl implements BookingFlowService {

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
    public ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpHeaders httpHeaders) throws Exception {
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

        ThreadLocalCache.testTracesThreadLocal.set(traces);
        ThreadLocalCache.testCaseIdThreadLocal.set(UUIDUtil.generateUUID());

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
        testCase.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");
        ThreadLocalCache.testCaseThreadLocal.set(testCase);


        /***************************
         * 2nd step: query ticket
         ***************************/
        QueryTicketRequestDto queryTicketRequestDto = ParamUtil.constructQueryTicketReqDto();
        List<QueryTicketResponseDto> queryTicketResponseDtos = testQueryTicket(headers, queryTicketRequestDto);
        // if query tickets is empty
        if (queryTicketResponseDtos == null || queryTicketResponseDtos.size() == 0) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
        }

        /*************************************
         * 3rd step: get contacts
         *************************************/
        List<Contact> contacts = testQueryContact(headers);
        // if query contacts is empty
        if (contacts == null || contacts.size() == 0) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
        }

        /***********************
         * 4th step: get food
         ***********************/
        // globe field to reuse
        String departureTime = queryTicketRequestDto.getDepartureTime();
        String startingStation = queryTicketRequestDto.getStartingPlace();
        String endingStation = queryTicketRequestDto.getEndPlace();
        // todo
        // 如果instance 错误，这个地方是得不到的 get 0
        String tripId = queryTicketResponseDtos.get(0).getTripId().getType()
                + queryTicketResponseDtos.get(0).getTripId().getNumber(); //默认选第一辆

        FoodRequestDto foodRequestDto = ParamUtil.constructFoodRequestDto(departureTime, startingStation,
                endingStation, tripId);
        FoodResponseDto foodResponseDto = testQueryFood(headers, foodRequestDto);
//        if (!foodResponseDto.isStatus()) {
//            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
//        }

        /******************************
         * 5th step: confirm ticket
         ******************************/
        String contactId = ParamUtil.getRandomContact(contacts);// random param
        ConfirmRequestDto confirmRequestDto = ParamUtil.constructConfirmRequestDto(departureTime, startingStation,
                endingStation, tripId, contactId);
        ConfirmResponseDto confirmResponseDto = testPreserveTicket(headers, confirmRequestDto);
        if (!confirmResponseDto.isStatus()) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
        }

        if (RandomUtil.getRandomTrueOrFalse()) {
            /*********************
             * 6th step: payment
             *********************/
            String orderId = confirmResponseDto.getOrder().getId().toString();
            PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
            boolean payResult = testTicketPayment(headers, paymentRequestDto);
            if (!payResult) {
                return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
            }


            if (RandomUtil.getRandomTrueOrFalse()) {
                /*****************************
                 * 7th step: collect ticket
                 *****************************/
                CollectRequestDto collectRequestDto = ParamUtil.constructCollectRequestDto(orderId);
                BasicMessage basicMessage = testTicketCollection(headers, collectRequestDto);
                if (!basicMessage.isStatus()) {
                    return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
                }

                if (RandomUtil.getRandomTrueOrFalse()) {
                    /****************************
                     * 8th step: enter station
                     ****************************/
                    ExcuteRequestDto excuteRequestDto = ParamUtil.constructExecuteRequestDto(orderId);
                    BasicMessage basicEnterStationMessage = testEnterStation(headers, excuteRequestDto);
                    if (!basicEnterStationMessage.isStatus()) {
                        return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
                    }
                }
            }
        }
        // construct response ----  passed all flow
        return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
    }

    public FlowTestResult returnFlowTestResult(List<TestTrace> testTraceList, TestCase testCase) {
        testCaseRepository.save(testCase);
        testTraceRepository.saveAll(testTraceList);
        FlowTestResult bftr = new FlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(testTraceList);
        return bftr;
    }


    private BasicMessage testEnterStation(Map<String, List<String>> headers, ExcuteRequestDto excuteRequestDto) {
        String enterTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), enterTraceId));

        ResponseEntity<BasicMessage> enterBasicMsgResp = enter(excuteRequestDto, headers);
        BasicMessage enterBasicMsg = enterBasicMsgResp.getBody();

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
        testTrace8.setTestClass("BookingFlowTestClass");
        testTrace8.setTestMethod("enter");
        testTrace8.setTestTraceId(enterTraceId);
        testTrace8.setError(0);
        testTrace8.setY_issue_ms("");
        testTrace8.setY_issue_dim_type("");
        testTrace8.setY_issue_dim_content("");
        // TODO
        // enterBasicMsg status false
        // ts-execute-service
        if (enterBasicMsg.getMessage() != null && enterBasicMsg.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("execute_execute");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(enterBasicMsg.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("execute_execute", totalAccessTime);
            } else {
                testTrace8.setError(1);
                testTrace8.setExpected_result(1);
                testTrace8.setY_issue_ms("ts-execute-service");
                testTrace8.setY_issue_dim_type("instance");
                testTrace8.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }


        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace8);
        System.out.println("----------" + testTrace8);
        return enterBasicMsg;
    }

    private BasicMessage testTicketCollection(Map<String, List<String>> headers, CollectRequestDto collectRequestDto) {
        String collectTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), collectTraceId));

        ResponseEntity<BasicMessage> collectBasicMsgResp = collect(collectRequestDto, headers);
        BasicMessage collectBasicMsg = collectBasicMsgResp.getBody();

        TestTrace testTrace7 = new TestTrace();
        testTrace7.setEntryApi("/execute/collected");
        testTrace7.setEntryService("ts-execute-service");
        testTrace7.setEntryTimestamp(System.currentTimeMillis());
        testTrace7.setExpected_result(0);
        try {
            testTrace7.setReq_param(objectMapper.writeValueAsString(collectRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace7.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace7.setTestClass("BookingFlowTestClass");
        testTrace7.setTestMethod("collect");
        testTrace7.setTestTraceId(collectTraceId);
        testTrace7.setError(0);
        testTrace7.setY_issue_ms("");
        testTrace7.setY_issue_dim_type("");
        testTrace7.setY_issue_dim_content("");
        // TODO
        // collectBasicMsg  status false
        // ts-execute-service
        if (collectBasicMsg.getMessage() != null && collectBasicMsg.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("execute_collected");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(collectBasicMsg.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("execute_collected", totalAccessTime);
            } else {
                testTrace7.setError(1);
                testTrace7.setExpected_result(1);
                testTrace7.setY_issue_ms("ts-execute-service");
                testTrace7.setY_issue_dim_type("instance");
                testTrace7.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace7);
        System.out.println("----------" + testTrace7);
        return collectBasicMsg;
    }

    private boolean testTicketPayment(Map<String, List<String>> headers, PaymentRequestDto paymentRequestDto) {
        String paymentTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), paymentTraceId));

        ResponseEntity<Boolean> paymentStatusResp = pay(paymentRequestDto, headers);
        boolean paymentStatus = paymentStatusResp.getBody();

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
        testTrace6.setTestClass("BookingFlowTestClass");
        testTrace6.setTestMethod("pay");
        testTrace6.setTestTraceId(paymentTraceId);
        testTrace6.setError(0);
        testTrace6.setY_issue_ms("");
        testTrace6.setY_issue_dim_type("");
        testTrace6.setY_issue_dim_content("");
        // todo
        // paymentStatus   是 false
        // ts-inside-payment-service
//        if (paymentStatus) {
//            TsServiceTestApplication.accessSuccessTimeMap.put("inside_payment_pay", TsServiceTestApplication.accessSuccessTimeMap.get("inside_payment_pay") + 1);
//            int accessSuccessTime = TsServiceTestApplication.accessSuccessTimeMap.get("inside_payment_pay");
//            if (accessSuccessTime > 5) {
//                testTrace6.setError(1);
//                testTrace6.setExpected_result(1);
//                testTrace6.setY_issue_ms("ts-inside-payment-service");
//                testTrace6.setY_issue_dim_type("instance");
//                testTrace6.setY_issue_dim_content("request more than 5 times");
//            }
//        }

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace6);
        System.out.println("----------" + testTrace6);
        return paymentStatus;
    }

    private ConfirmResponseDto testPreserveTicket(Map<String, List<String>> headers, ConfirmRequestDto
            confirmRequestDto) {
        String confirmTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), confirmTraceId));

        ResponseEntity<ConfirmResponseDto> confirmResponseDtoResp = preserve(confirmRequestDto, headers);
        ConfirmResponseDto confirmResponseDto = confirmResponseDtoResp.getBody();

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setEntryApi("/preserve");
        testTrace5.setEntryService("ts-preserve-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        testTrace5.setExpected_result(0);
        try {
            testTrace5.setReq_param(objectMapper.writeValueAsString(confirmRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace5.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace5.setTestClass("BookingFlowTestClass");
        testTrace5.setTestMethod("preserve");
        testTrace5.setTestTraceId(confirmTraceId);
        testTrace5.setError(0);
        testTrace5.setY_issue_ms("");
        testTrace5.setY_issue_dim_type("");
        testTrace5.setY_issue_dim_content("");

        // todo
        // confirmResponseDto 返回 status  false
        // ts-preserve-service
        if (confirmResponseDto.getMessage() != null && confirmResponseDto.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("preserve");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(confirmResponseDto.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("preserve", totalAccessTime);
            } else {
                testTrace5.setError(1);
                testTrace5.setExpected_result(1);
                testTrace5.setY_issue_ms("ts-preserve-service");
                testTrace5.setY_issue_dim_type("instance");
                testTrace5.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace5);
        System.out.println("----------" + testTrace5);
        return confirmResponseDto;
    }

    private FoodResponseDto testQueryFood(Map<String, List<String>> headers, FoodRequestDto foodRequestDto) {
        String foodTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), foodTraceId));

        ResponseEntity<FoodResponseDto> foodResponseDtoResp = getFood(foodRequestDto, headers);
        FoodResponseDto foodResponseDto = foodResponseDtoResp.getBody();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/food/getFood");
        testTrace4.setEntryService("ts-food-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        try {
            testTrace4.setReq_param(objectMapper.writeValueAsString(foodRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace4.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace4.setTestClass("BookingFlowTestClass");
        testTrace4.setTestMethod("getFood");
        testTrace4.setTestTraceId(foodTraceId);
        testTrace4.setExpected_result(0);
        testTrace4.setError(0);
        testTrace4.setY_issue_ms("");
        testTrace4.setY_issue_dim_type("");
        testTrace4.setY_issue_dim_content("");

        // todo
        // 判断 foodResponseDto 的status 是否为false
        if (foodResponseDto.getMessage() != null && foodResponseDto.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("food_getFood");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(foodResponseDto.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);

            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("food_getFood", totalAccessTime);
            } else {
                testTrace4.setError(1);
                testTrace4.setExpected_result(1);
                testTrace4.setY_issue_ms("ts-food-service");
                testTrace4.setY_issue_dim_type("instance");
                testTrace4.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace4);
        System.out.println("----------" + testTrace4);
        return foodResponseDto;
    }

    private List<Contact> testQueryContact(Map<String, List<String>> headers) {
        String contactTraceId = UUIDUtil.generateUUID();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), contactTraceId));

        ResponseEntity<List<Contact>> contactsResp = getContacts(headers);
        List<Contact> contacts = contactsResp.getBody();

        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/contacts/findContacts");
        testTrace3.setEntryService("ts-contacts-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        testTrace3.setExpected_result(0);
        try {
            testTrace3.setReq_param(objectMapper.writeValueAsString(null));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace3.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace3.setTestClass("BookingFlowTestClass");
        testTrace3.setTestMethod("getContacts");
        testTrace3.setTestTraceId(contactTraceId);
        testTrace3.setError(0);
        testTrace3.setY_issue_ms("");
        testTrace3.setY_issue_dim_type("");
        testTrace3.setY_issue_dim_content("");


        // todo
        //ts-contacts-service
        if (contacts != null && contacts.size() > 0) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("contacts_findContacts");
            // 服务端返回值
            int totalAccessTime = contacts.get(contacts.size() - 1).getDocumentType();
            System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("contacts_findContacts", totalAccessTime);
            } else {
                testTrace3.setError(1);
                testTrace3.setExpected_result(1);
                testTrace3.setY_issue_ms("ts-contacts-service");
                testTrace3.setY_issue_dim_type("instance");
                testTrace3.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }
        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace3);
        System.out.println("----------" + testTrace3);
        return contacts;
    }

    private List<QueryTicketResponseDto> testQueryTicket(Map<String, List<String>> headers, QueryTicketRequestDto
            queryTicketRequestDto) {
        String queryTicketTraceId = UUIDUtil.generateUUID();

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), queryTicketTraceId));

        ResponseEntity<List<QueryTicketResponseDto>> queryTicketResponseDtosResp = queryTicket(queryTicketRequestDto,
                headers);
        List<QueryTicketResponseDto> queryTicketResponseDtos = queryTicketResponseDtosResp.getBody();

        TestTrace testTrace2 = new TestTrace();

        testTrace2.setEntryTimestamp(System.currentTimeMillis());

        try {
            testTrace2.setReq_param(objectMapper.writeValueAsString(queryTicketRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace2.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace2.setTestClass("BookingFlowTestClass");
        testTrace2.setTestMethod("queryTicket");
        testTrace2.setTestTraceId(queryTicketTraceId);
        if (queryTicketRequestDto.getEndPlace().equals(ServiceConstant.NAN_JING)) {
            testTrace2.setEntryApi("/travel2/query");
            testTrace2.setEntryService("ts-travel2-service");
            testTrace2.setError(0);
            testTrace2.setY_issue_ms("");
            testTrace2.setY_issue_dim_type("");
            testTrace2.setY_issue_dim_content("");
            // todo
            //ts-travel-service
            // 客户端记录值
            if (queryTicketResponseDtos != null && queryTicketResponseDtos.size() > 0) {
                int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("travel2_query");
                int totalAccessTime = Integer.parseInt(queryTicketResponseDtos.get(queryTicketResponseDtos.size() - 1).getTrainTypeId());

                System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);

                if (totalAccessTime > clientAccessTime) {
                    TsServiceTestApplication.clientAccessTimeMap.put("travel2_query", totalAccessTime);
                } else {
                    testTrace2.setError(1);
                    testTrace2.setExpected_result(1);
                    testTrace2.setY_issue_ms("ts-travel2-service");
                    testTrace2.setY_issue_dim_type("instance");
                    testTrace2.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">" + clientAccessTime + "totalAccessTime");
                }
            }
        } else {
            testTrace2.setEntryApi("/travel/query");
            testTrace2.setEntryService("ts-travel-service");
            testTrace2.setError(0);
            testTrace2.setExpected_result(0);
            testTrace2.setY_issue_ms("");
            testTrace2.setY_issue_dim_type("");
            testTrace2.setY_issue_dim_content("");
            // todo
            //ts-travel-service
            if (queryTicketResponseDtos != null && queryTicketResponseDtos.size() > 0) {
                // 客户端记录值
                int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("travel_query");
                int totalAccessTime = Integer.parseInt(queryTicketResponseDtos.get(queryTicketResponseDtos.size() - 1).getTrainTypeId());

                System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);

                if (totalAccessTime > clientAccessTime) {
                    TsServiceTestApplication.clientAccessTimeMap.put("travel_query", totalAccessTime);
                } else {
                    testTrace2.setError(1);
                    testTrace2.setExpected_result(1);
                    testTrace2.setY_issue_ms("ts-travel-service");
                    testTrace2.setY_issue_dim_type("instance");
                    testTrace2.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
                }
            }
        }

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace2);

        System.out.println("----------" + testTrace2);
        return queryTicketResponseDtos;
    }

    private LoginResponseDto testLogin(LoginRequestDto loginRequestDto) {
        String loginTraceId = UUIDUtil.generateUUID();

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");

        loginHeaders.add(ServiceConstant.USER_AGENT, ThreadLocalCache.testCaseIdThreadLocal.get() + "," + loginTraceId);

        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        try {
            ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
            loginResponseDto = loginResponseDtoResp.getBody();
        } catch (Exception e) {
            loginResponseDto.setStatus(false);
        }

        TestTrace testTrace = new TestTrace();
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());

        testTrace.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace.setTestClass("BookingFlowTestClass");
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(loginTraceId);
        try {
            testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        testTrace.setError(0);
        testTrace.setExpected_result(0);
        testTrace.setY_issue_ms("");
        testTrace.setY_issue_dim_type("");
        testTrace.setY_issue_dim_content("");

        // todo
        //ts-login-service
        if (loginResponseDto.getMessage() != null && loginResponseDto.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("login");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(loginResponseDto.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------"+ clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("login", totalAccessTime);
            } else {
                testTrace.setError(1);
                testTrace.setExpected_result(1);
                testTrace.setY_issue_ms("ts-login-service");
                testTrace.setY_issue_dim_type("instance");
                testTrace.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace);
        System.out.println("------- " + testTrace);
        return loginResponseDto;
    }

}
