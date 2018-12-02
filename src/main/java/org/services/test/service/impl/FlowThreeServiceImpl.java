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
import org.services.test.exception.UnknownException;
import org.services.test.repository.TestCaseRepository;
import org.services.test.repository.TestTraceRepository;
import org.services.test.service.FlowThreeService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class FlowThreeServiceImpl implements FlowThreeService {

    @Autowired
    BookingFlowServiceImpl bookingFlowServiceImpl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClusterConfig clusterConfig;

    @Autowired
    private TestTraceRepository testTraceRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;


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
    public List<Order> queryOrders(OrderQueryRequestDto orderQueryRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> requestBody = new HttpEntity<>(orderQueryRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/order/query");
        ResponseEntity<List<Order>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, new
                ParameterizedTypeReference<List<Order>>() {
                });

        return responseEntity.getBody();
    }

    @Override
    public List<Order> queryOrdersOther(OrderQueryRequestDto orderQueryRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> requestBody = new HttpEntity<>(orderQueryRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/orderOther/query");
        ResponseEntity<List<Order>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, new
                ParameterizedTypeReference<List<Order>>() {
                });

        return responseEntity.getBody();
    }

    @Override
    public StationNameResponseDto queryStationNameById(StationNameRequestDto stationNameRequestDto, Map<String,
            List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<StationNameRequestDto> requestBody = new HttpEntity<>(stationNameRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/station/queryById");
        ResponseEntity<StationNameResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                requestBody, StationNameResponseDto.class);

        return responseEntity.getBody();
    }

    @Override
    public ConsignInsertResponseDto consignOrder(ConsignInsertRequestDto consignInsertRequestDto, Map<String,
            List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<ConsignInsertRequestDto> requestBody = new HttpEntity<>(consignInsertRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/consign/insertConsign");
        ResponseEntity<ConsignInsertResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                requestBody, ConsignInsertResponseDto.class);

        return responseEntity.getBody();
    }

    @Override
    public List<ConsignInsertRequestDto> queryConsignedOrders(String accountId, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(),
                "/consign/findByAccountId/" + accountId);
        ResponseEntity<List<ConsignInsertRequestDto>> responseEntity = restTemplate.exchange(url, HttpMethod.GET,
                requestBody, new ParameterizedTypeReference<List<ConsignInsertRequestDto>>() {
                });

        return responseEntity.getBody();
    }

    @Override
    public Object getVoucherHtml(VoucherUIRequestDto voucherUIRequestDto, Map<String, List<String>> headers) {

        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        //httpHeaders.setAccept(Arrays.asList(MediaType.TEXT_HTML));
        //httpHeaders.setContentType(MediaType.TEXT_HTML);
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/voucher.html");
        String param = "?orderId=" + voucherUIRequestDto.getOrderId() + "&train_number=" + voucherUIRequestDto
                .getTrain_number();
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        try {
            responseEntity = restTemplate.exchange(url + param, HttpMethod.GET, requestBody, String.class);
        } catch (Exception e) {
            if (e.getMessage().contains("JsonParseException")) {
                return responseEntity.getBody();
            }

            throw new UnknownException(e.getMessage());
        }

        return responseEntity.getBody();
    }

    @Override
    public VoucherInfoResponseDto getVoucherInfo(VoucherInfoRequestDto voucherInfoRequestDto, Map<String,
            List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<VoucherInfoRequestDto> requestBody = new HttpEntity<>(voucherInfoRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/getVoucher");
        ResponseEntity<VoucherInfoResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST,
                requestBody, new ParameterizedTypeReference<VoucherInfoResponseDto>() {
                });

        return responseEntity.getBody();
    }

    @Override
    public FlowTestResult consignFlow() throws Exception {
        List<TestTrace> traces = new ArrayList<>();
        ThreadLocalCache.testTracesThreadLocal.set(traces);
        ThreadLocalCache.testCaseIdThreadLocal.set(UUIDUtil.generateUUID());

        FlowTestResult flowTestResult = new FlowTestResult();

        Map<String, List<String>> headers = new HashMap<>();

        /*
         * Get orders, contain steps:
         *     1. login
         *     2. query order service
         *     3. query order other service
         */
        List<Order> orders = getOrders(headers);
        if (CollectionUtils.isEmpty(orders)) {
            saveDataForReturn(flowTestResult);
            return flowTestResult;
        }

        /*
         * Query station name by id, contain steps:
         *     4. query station name by id
         */
        queryStationNameById(headers, orders);


        /*
         * 5. consign order
         */
        List<Order> consignableOrders = orders.stream().filter(
                order -> 0 == order.getStatus()
                        || 1 == order.getStatus()
                        || 2 == order.getStatus()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(consignableOrders)) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), ThreadLocalCache.testCaseThreadLocal.get());
        }

        Order consignableOrder = RandomUtil.getRandomElementInList(consignableOrders);
        ConsignInsertRequestDto consignInsertRequestDto = ParamUtil.constructConsignRequestDto(consignableOrder);
        testConsignOrder(headers, consignInsertRequestDto);


        /*
         * 6. view consigned order
         */
        String accountId = consignableOrder.getAccountId().toString();
        List<ConsignInsertRequestDto> consignOrders = testQueryConsignOrder(headers, accountId);


        /*
         * 7. query station name by id
         */
        StationNameRequestDto stationNameRequestDto;
        for (ConsignInsertRequestDto consignOrder : consignOrders) {
            stationNameRequestDto = ParamUtil.constructStationNameRequestDto(consignOrder.getFrom());
            testQueryStationNameById(headers, stationNameRequestDto);

            stationNameRequestDto = ParamUtil.constructStationNameRequestDto(consignOrder.getTo());
            testQueryStationNameById(headers, stationNameRequestDto);
        }

        // todo
      return  returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), ThreadLocalCache.testCaseThreadLocal.get());
    }


    // =====================================================================
    // =====================================================================
    // =====================================================================
    // =====================================================================


    @Override
    public FlowTestResult voucherFlow() throws Exception {
        ThreadLocalCache.testTracesThreadLocal.set(new ArrayList<>());
        ThreadLocalCache.testCaseIdThreadLocal.set(UUIDUtil.generateUUID());

        FlowTestResult flowTestResult = new FlowTestResult();

        Map<String, List<String>> headers = new HashMap<>();


        /*
         * Get orders, contain steps:
         *     1. login
         *     2. query order service
         *     3. query order service
         */
        List<Order> orders = getOrders(headers);
        if (CollectionUtils.isEmpty(orders)) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), ThreadLocalCache.testCaseThreadLocal.get());
        }


        /*
         * Query station name by id, contain steps:
         *     4. query station name by id
         */
        // todo  好像不能加num
        queryStationNameById(headers, orders);


        /*
         * 5. Print voucher, get voucher html
         */
        List<Order> voucherableOrders = orders.stream().filter(
                order -> 6 == order.getStatus()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(voucherableOrders)) {
            return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), ThreadLocalCache.testCaseThreadLocal.get());
        }

        Order voucherableOrder = RandomUtil.getRandomElementInList(voucherableOrders);
        VoucherUIRequestDto voucherUIRequestDto = ParamUtil.constructVoucherUIRequestDto(voucherableOrder);
        testGetVoucherHtml(headers, voucherUIRequestDto);


        /*
         * 6. get voucher information
         */
        VoucherInfoRequestDto voucherInfoRequestDto = ParamUtil.constructVoucherInfoRequestDto(voucherableOrder);
        testGetVoucherInfo(headers, voucherInfoRequestDto);

        // construct response ----  passed all flow
        return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), ThreadLocalCache.testCaseThreadLocal.get());
    }

    @Transactional
    public FlowTestResult returnFlowTestResult(List<TestTrace> testTraceList, TestCase testCase) {
        // save data to mysql
        testCaseRepository.save(testCase);
        testTraceRepository.saveAll(testTraceList);

        FlowTestResult bftr = new FlowTestResult();
        bftr.setTestCase(testCase);
        bftr.setTestTraces(testTraceList);
        return bftr;
    }


    private List<Order> testQueryOrders(Map<String, List<String>> headers, OrderQueryRequestDto orderQueryRequestDto)
            throws Exception {
        String queryOrdersTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace1 = new TestTrace();
        testTrace1.setEntryApi("/order/query");
        testTrace1.setEntryService("ts-order-service");
        testTrace1.setEntryTimestamp(System.currentTimeMillis());
        testTrace1.setExpected_result(0);
        testTrace1.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace1.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace1.setTestMethod("queryOrders");
        testTrace1.setReq_param(objectMapper.writeValueAsString(orderQueryRequestDto));
        testTrace1.setTestTraceId(queryOrdersTraceId);
        testTrace1.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace1);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                queryOrdersTraceId));

        List<Order> orderList = queryOrders(orderQueryRequestDto, headers);
        // todo
        //ts-travel-service
        if (orderList != null && orderList.size() > 0) {
            // 客户端记录值
            // todo  在order 里面 加一个字段
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("travel_query");
            int totalAccessTime = 1;
           // int totalAccessTime = Integer.parseInt(orderList.get(orderList.size() - 1).getTrainTypeId());

            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);

            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("travel_query", totalAccessTime);
            } else {
                // 此处为引用
                testTrace1 = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace1.setError(1);
                testTrace1.setExpected_result(1);
                testTrace1.setY_issue_ms("ts-order-service");
                testTrace1.setY_issue_dim_type("instance");
                testTrace1.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }
        return orderList;
    }

    private List<Order> testQueryOrdersOther(Map<String, List<String>> headers, OrderQueryRequestDto
            orderQueryRequestDto) throws Exception {
        String queryOrdersOtherTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace2 = new TestTrace();
        testTrace2.setEntryApi("/orderOther/query");
        testTrace2.setEntryService("ts-order-other-service");
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setExpected_result(0);
        testTrace2.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace2.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace2.setReq_param(objectMapper.writeValueAsString(orderQueryRequestDto));
        testTrace2.setTestMethod("queryOrdersOther");
        testTrace2.setTestTraceId(queryOrdersOtherTraceId);
        testTrace2.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace2);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                queryOrdersOtherTraceId));
        List<Order> otherOrderList = queryOrdersOther(orderQueryRequestDto, headers);

         // todo order 里面加个值
        // 客户端记录值
        if (otherOrderList != null && otherOrderList.size() > 0) {
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("travel2_query");
            int totalAccessTime = 1;
           // int totalAccessTime = Integer.parseInt(otherOrderList.get(otherOrderList.size() - 1).getTrainTypeId());

            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);

            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("travel2_query", totalAccessTime);
            } else {
                // 此处为引用
                testTrace2 = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace2.setError(1);
                testTrace2.setExpected_result(1);
                testTrace2.setY_issue_ms("ts-order-other-service");
                testTrace2.setY_issue_dim_type("instance");
                testTrace2.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">" + clientAccessTime + "totalAccessTime");
            }
        }

        return otherOrderList;
    }

    private void testQueryStationNameById(Map<String, List<String>> headers, StationNameRequestDto
            stationNameRequestDto) throws Exception {
        String queryStationNameTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/station/queryById");
        testTrace3.setEntryService("ts-station-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        testTrace3.setExpected_result(0);
        testTrace3.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace3.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace3.setReq_param(objectMapper.writeValueAsString(stationNameRequestDto));
        testTrace3.setTestMethod("queryStationNameById");
        testTrace3.setTestTraceId(queryStationNameTraceId);
        testTrace3.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace3);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                queryStationNameTraceId));
        queryStationNameById(stationNameRequestDto, headers);




    }

    private void testConsignOrder(Map<String, List<String>> headers, ConsignInsertRequestDto consignInsertRequestDto)
            throws Exception {
        String consignOrderTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/consign/insertConsign");
        testTrace4.setEntryService("ts-consign-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        testTrace4.setExpected_result(0);
        testTrace4.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace4.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace4.setReq_param(objectMapper.writeValueAsString(consignInsertRequestDto));
        testTrace4.setTestMethod("consignOrder");
        testTrace4.setTestTraceId(consignOrderTraceId);
        testTrace4.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace4);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                consignOrderTraceId));
        consignOrder(consignInsertRequestDto, headers);
    }

    private List<ConsignInsertRequestDto> testQueryConsignOrder(Map<String, List<String>> headers, String accountId)
            throws Exception {
        String searchConsignOrderTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace5 = new TestTrace();
        testTrace5.setEntryApi("/consign/findByAccountId/" + accountId);
        testTrace5.setEntryService("ts-consign-service");
        testTrace5.setEntryTimestamp(System.currentTimeMillis());
        testTrace5.setExpected_result(0);
        testTrace5.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace5.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace5.setReq_param(objectMapper.writeValueAsString(accountId));
        testTrace5.setTestMethod("queryConsignedOrders");
        testTrace5.setTestTraceId(searchConsignOrderTraceId);
        testTrace5.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace5);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                searchConsignOrderTraceId));
        return queryConsignedOrders(accountId, headers);
    }

    private void testGetVoucherHtml(Map<String, List<String>> headers, VoucherUIRequestDto voucherUIRequestDto)
            throws Exception {
        String getVoucherHtmlTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace6 = new TestTrace();
        testTrace6.setEntryApi("/voucher.html?orderId="
                + voucherUIRequestDto.getOrderId() + "&train_number=" + voucherUIRequestDto.getTrain_number());
        testTrace6.setEntryService("ts-voucher-service");
        testTrace6.setEntryTimestamp(System.currentTimeMillis());
        testTrace6.setExpected_result(0);
        testTrace6.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace6.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace6.setReq_param(objectMapper.writeValueAsString(voucherUIRequestDto));
        testTrace6.setTestMethod("getVoucherHtml");
        testTrace6.setTestTraceId(getVoucherHtmlTraceId);
        testTrace6.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace6);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                getVoucherHtmlTraceId));
        getVoucherHtml(voucherUIRequestDto, headers);
    }

    private void testGetVoucherInfo(Map<String, List<String>> headers, VoucherInfoRequestDto voucherInfoRequestDto)
            throws Exception {
        String getVoucherInfoTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace7 = new TestTrace();
        testTrace7.setEntryApi("/getVoucher");
        testTrace7.setEntryService("ts-voucher-service");
        testTrace7.setEntryTimestamp(System.currentTimeMillis());
        testTrace7.setExpected_result(0);
        testTrace7.setTestClass(ServiceConstant.CONSIGN_FLOW);
        testTrace7.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace7.setReq_param(objectMapper.writeValueAsString(voucherInfoRequestDto));
        testTrace7.setTestMethod("getVoucherInfo");
        testTrace7.setTestTraceId(getVoucherInfoTraceId);
        testTrace7.setSequence(TestTraceUtil.getTestTraceSequence());

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace7);

        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(),
                getVoucherInfoTraceId));
        getVoucherInfo(voucherInfoRequestDto, headers);
    }

    private List<Order> getOrders(Map<String, List<String>> headers) throws Exception {

        /*
         * 1. login
         */
        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();
        LoginResponseDto loginResponseDto = testLogin(loginRequestDto);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        headers.putAll(loginResponseDto.getHeaders());

        // construct test case info
        // construct test case info
        TestCase testCase = new TestCase();
        testCase.setUserId(loginRequestDto.getEmail());
        testCase.setSessionId(headers.get(ServiceConstant.COOKIE).toString());
        testCase.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testCase.setUserDetail("user details");
        testCase.setUserType("normal");
        ThreadLocalCache.testCaseThreadLocal.set(testCase);


        /*
         * 2. query order service
         */
        OrderQueryRequestDto orderQueryRequestDto = ParamUtil.constructOrderQueryRequestDto();
        List<Order> orders = testQueryOrders(headers, orderQueryRequestDto);


        /*
         * 3. query order service
         */
        OrderQueryRequestDto orderOtherQueryRequestDto = ParamUtil.constructOrderQueryRequestDto();
        List<Order> ordersOther = testQueryOrdersOther(headers, orderOtherQueryRequestDto);

        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(orders);
        allOrders.addAll(ordersOther);

        return allOrders;
    }

    LoginResponseDto testLogin(LoginRequestDto loginRequestDto) throws JsonProcessingException {
        String loginTraceId = UUIDUtil.generateUUID();

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.add(ServiceConstant.USER_AGENT, ServiceConstant.TEST_CASE_ID + ":" + ThreadLocalCache
                .testCaseIdThreadLocal.get());
        loginHeaders.add(ServiceConstant.USER_AGENT, ServiceConstant.TEST_TRACE_ID + ":" + loginTraceId);
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        TestTrace testTrace = new TestTrace();
        testTrace.setError(0);
        testTrace.setEntryApi("/login");
        testTrace.setEntryService("ts-login-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setSequence(TestTraceUtil.getTestTraceSequence());
        testTrace.setExpected_result(0);
        testTrace.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace.setTestClass("BookingFlowTestClass");
        testTrace.setTestMethod("login");
        testTrace.setTestTraceId(loginTraceId);
        testTrace.setReq_param(objectMapper.writeValueAsString(loginRequestDto));
        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace);




        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
        LoginResponseDto loginResponseDto = loginResponseDtoResp.getBody();

        // todo
        //ts-login-service
        if (loginResponseDto.getMessage() != null && loginResponseDto.getMessage().contains("__")) {
            // 客户端记录值
            int clientAccessTime = TsServiceTestApplication.clientAccessTimeMap.get("login");
            // 服务端返回值
            int totalAccessTime = Integer.parseInt(loginResponseDto.getMessage().split("__")[1]);
            System.out.println(totalAccessTime + "----23333333------" + clientAccessTime);
            if (totalAccessTime > clientAccessTime) {
                TsServiceTestApplication.clientAccessTimeMap.put("login", totalAccessTime);
            } else {
                // 此处为引用
                testTrace = ThreadLocalCache.testTracesThreadLocal.get().get(ThreadLocalCache.testTracesThreadLocal.get().size() - 1);
                testTrace.setError(1);
                testTrace.setExpected_result(1);
                testTrace.setY_issue_ms("ts-login-service");
                testTrace.setY_issue_dim_type("instance");
                testTrace.setY_issue_dim_content("clientAccessTime" + clientAccessTime + ">=" + clientAccessTime + "totalAccessTime");
            }
        }

        return loginResponseDto;
    }

    private void queryStationNameById(Map<String, List<String>> headers, List<Order> orders) throws Exception {
        /*
         * 4. query station name by id
         */
        StationNameRequestDto stationNameRequestDto;
        Order order = RandomUtil.getRandomElementInList(orders);
        stationNameRequestDto = ParamUtil.constructStationNameRequestDto(order.getFrom());
        testQueryStationNameById(headers, stationNameRequestDto);

        stationNameRequestDto = ParamUtil.constructStationNameRequestDto(order.getTo());
        testQueryStationNameById(headers, stationNameRequestDto);

    }

    private void saveDataForReturn(FlowTestResult flowTestResult) {
        flowTestResult.setTestCase(ThreadLocalCache.testCaseThreadLocal.get());
        flowTestResult.setTestTraces(ThreadLocalCache.testTracesThreadLocal.get());
        // todo

//        bookingFlowServiceImpl.persistTestData(flowTestResult.getTestCase(), flowTestResult.getTestTraces());
    }

}
