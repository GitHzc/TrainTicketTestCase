package org.services.test.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.config.ClusterConfig;

import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.*;
import org.services.test.repository.TestCaseRepository;
import org.services.test.repository.TestTraceRepository;
import org.services.test.service.AdminOrderFlowService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class AdminOrderFlowServiceImpl implements AdminOrderFlowService {


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
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/adminorder/findAll/" + adminId);
        ResponseEntity<GetAllOrderResultDto> ret = restTemplate.exchange(url, HttpMethod.GET, requestBody, GetAllOrderResultDto.class);
        return ret;
    }

    @Override
    public ResponseEntity<AddOrderResultDto> adminAddOrder(AddOrderRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<AddOrderRequestDto> requestBody = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/adminorder/addOrder");
        ResponseEntity<AddOrderResultDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, AddOrderResultDto.class);
        return responseEntity;
    }

    @Override
    public ResponseEntity<UpdateOrderResultDto> adminUpdateOrder(UpdateOrderRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<UpdateOrderRequestDto> requestBody = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/adminorder/updateOrder");
        ResponseEntity<UpdateOrderResultDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, UpdateOrderResultDto.class);
        return responseEntity;
    }

    @Override
    public ResponseEntity<DeleteOrderResultDto> adminDeleteOrder(DeleteOrderRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<DeleteOrderRequestDto> requestBody = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/adminorder/deleteOrder");
        ResponseEntity<DeleteOrderResultDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, DeleteOrderResultDto.class);
        return responseEntity;
    }

    @Override
    public ResponseEntity<GetRoutesListlResultDto> getAllRoutesResult(String adminId, Map<String, List<String>> headers) {

        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/adminroute/findAll/" + adminId);
        ResponseEntity<GetRoutesListlResultDto> ret = restTemplate.exchange(url, HttpMethod.GET, requestBody, GetRoutesListlResultDto.class);
        return ret;
    }

    @Override
    public ResponseEntity<FindAllTravelResultDto> getAllTravelResult(String adminId, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/admintravel/findAll/" + adminId);
        ResponseEntity<FindAllTravelResultDto> ret = restTemplate.exchange(url, HttpMethod.GET, requestBody, FindAllTravelResultDto.class);
        return ret;
    }

    /**
     * describe: login -> get all orders  => (get one  update ) : (add ->  update) => if: delete -> if: query route -> if: query trave
     *
     * @return
     * @throws Exception
     */

    @Override
    public FlowTestResult adminOrderFlow() throws Exception {


        List<TestTrace> traces = new ArrayList<>();

        ThreadLocalCache.testTracesThreadLocal.set(traces);
        ThreadLocalCache.testCaseIdThreadLocal.set(UUIDUtil.generateUUID());

        /******************
         * 1st step: login
         *****************/
        AdminLoginInfoDto adminLoginInfoDto = ParamUtil.constructAdminLoginRequestDto();
        Contact contact = testAdminLogin(adminLoginInfoDto);


        /**
         * construct test_case
         */
        TestCase testCase = new TestCase();
        testCase.setUserId(contact.getId());
        testCase.setSessionId("");
        testCase.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testCase.setUserDetail(contact.getName());
        testCase.setUserType("admin");
        ThreadLocalCache.testCaseThreadLocal.set(testCase);

        /******************
         * 2st step: query orders
         *****************/

        GetAllOrderResultDto allOrderListDto = testGetAllOrdersResult(contact.getId());

        /**
         * if true:  update from list
         * else  new add -> update
         */
        UpdateOrderResultDto updateOrderResultDto = null;
        if (RandomUtil.getRandomTrueOrFalse()) {
            if (allOrderListDto.isStatus() == false || allOrderListDto.getOrders().size() == 0) {
                return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
            }
            /******************
             * 3st step: update orders
             *****************/
            UpdateOrderRequestDto dpOrderDto = new UpdateOrderRequestDto();
            dpOrderDto.setLoginid(contact.getId());
            dpOrderDto.setOrder(allOrderListDto.getOrders().get(0));
            updateOrderResultDto = testAdminUpdateOrder(dpOrderDto);

        } else {
            /******************
             * 3st step: add orders
             *****************/
            AddOrderRequestDto addOrderRequestDto = ParamUtil.constructAdminAddOrder(contact);
            AddOrderResultDto adminAddOrderResult = testAdminAddOrder(addOrderRequestDto);

            /******************
             * 4st step: update orders
             *****************/

            if (adminAddOrderResult.isStatus()) {
                UpdateOrderRequestDto dpOrderDto = new UpdateOrderRequestDto();
                dpOrderDto.setLoginid(contact.getId());
                dpOrderDto.setOrder(adminAddOrderResult.getOrder());
                updateOrderResultDto = testAdminUpdateOrder(dpOrderDto);
            }
        }


        if (RandomUtil.getRandomTrueOrFalse()) {
            /******************
             * 5st step: delete orders
             *****************/

            DeleteOrderResultDto deleteOrderResultDto = null;
            if (updateOrderResultDto.isStatus()) {
                DeleteOrderRequestDto deleteOrderRequestDto = new DeleteOrderRequestDto();
                deleteOrderRequestDto.setLoginid(contact.getId());
                deleteOrderRequestDto.setOrderId(updateOrderResultDto.getOrder().getId() + "");
                deleteOrderRequestDto.setTrainNumber(updateOrderResultDto.getOrder().getTrainNumber());
                deleteOrderResultDto = testAdminDeleteOrder(deleteOrderRequestDto);
            }
        }
        if (RandomUtil.getRandomTrueOrFalse()) {
            /******************
             * 6st step: query routes
             *****************/
            testGetAllRoutes(contact.getId());
        }
        if (RandomUtil.getRandomTrueOrFalse()) {
            /******************
             * 7st step: query travels
             *****************/
            testGetAllTravelResult(contact.getId());
        }

        return returnFlowTestResult(ThreadLocalCache.testTracesThreadLocal.get(), testCase);
    }

    private FindAllTravelResultDto testGetAllTravelResult(String amindid) throws Exception {
        String getAllTravelTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace7 = new TestTrace();
        testTrace7.setEntryApi("/admintravel/findAll");
        testTrace7.setEntryService("ts-admin-travel-service");
        testTrace7.setEntryTimestamp(System.currentTimeMillis());
        testTrace7.setExpected_result(0);
        testTrace7.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace7.setTestClass("AdminOrderFlowClass");
        testTrace7.setTestMethod("getAllTravelResult");
        testTrace7.setTestTraceId(getAllTravelTraceId);
        testTrace7.setReq_param(objectMapper.writeValueAsString(amindid));

        // todo
        testTrace7.setError(0);
        testTrace7.setY_issue_ms("");
        testTrace7.setY_issue_dim_type("");
        testTrace7.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace7);


        HttpHeaders headers = new HttpHeaders();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), getAllTravelTraceId));

        ResponseEntity<FindAllTravelResultDto> getAllTraveEntity = getAllTravelResult(amindid, headers);
        FindAllTravelResultDto allTravesResult = getAllTraveEntity.getBody();

        return allTravesResult;
    }

    private GetRoutesListlResultDto testGetAllRoutes(String amindid) throws Exception {
        String getAllRouteTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace6 = new TestTrace();
        testTrace6.setEntryApi("/adminroute/findAll");
        testTrace6.setEntryService("ts-admin-route-service");
        testTrace6.setEntryTimestamp(System.currentTimeMillis());
        testTrace6.setExpected_result(0);
        testTrace6.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace6.setTestClass("AdminOrderFlowClass");
        testTrace6.setTestMethod("getAllRoutesResult");
        testTrace6.setTestTraceId(getAllRouteTraceId);
        testTrace6.setReq_param(objectMapper.writeValueAsString(amindid));

        // todo
        testTrace6.setError(0);
        testTrace6.setY_issue_ms("");
        testTrace6.setY_issue_dim_type("");
        testTrace6.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace6);


        HttpHeaders headers = new HttpHeaders();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), getAllRouteTraceId));

        ResponseEntity<GetRoutesListlResultDto> getAllRouteEntity = getAllRoutesResult(amindid, headers);
        GetRoutesListlResultDto allRoutesResult = getAllRouteEntity.getBody();

        return allRoutesResult;
    }

    private DeleteOrderResultDto testAdminDeleteOrder(DeleteOrderRequestDto dto) throws Exception {
        String adminDeleteOrderTraceId = UUIDUtil.generateUUID();


        TestTrace testTrace6 = new TestTrace();
        testTrace6.setEntryApi("/adminorder/deleteOrder");
        testTrace6.setEntryService("ts-admin-order-service");
        testTrace6.setEntryTimestamp(System.currentTimeMillis());
        testTrace6.setExpected_result(0);
        testTrace6.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace6.setTestClass("AdminOrderFlowClass");
        testTrace6.setTestMethod("adminDeleteOrder");
        testTrace6.setTestTraceId(adminDeleteOrderTraceId);
        testTrace6.setReq_param(objectMapper.writeValueAsString(dto));

        // todo
        testTrace6.setError(0);
        testTrace6.setY_issue_ms("");
        testTrace6.setY_issue_dim_type("");
        testTrace6.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace6);


        HttpHeaders headers = new HttpHeaders();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), adminDeleteOrderTraceId));

        ResponseEntity<DeleteOrderResultDto> deleteOrderResultEntity = adminDeleteOrder(dto, headers);
        DeleteOrderResultDto deleteOrderResult = deleteOrderResultEntity.getBody();

        return deleteOrderResult;
    }

    private UpdateOrderResultDto testAdminUpdateOrder(UpdateOrderRequestDto dto) throws Exception {
        String adminUpdateOrderTraceId = UUIDUtil.generateUUID();


        TestTrace testTrace4 = new TestTrace();
        testTrace4.setEntryApi("/adminorder/updateOrder");
        testTrace4.setEntryService("ts-admin-order-service");
        testTrace4.setEntryTimestamp(System.currentTimeMillis());
        testTrace4.setExpected_result(0);
        testTrace4.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace4.setTestClass("AdminOrderFlowClass");
        testTrace4.setTestMethod("adminUpdateOrder");
        testTrace4.setTestTraceId(adminUpdateOrderTraceId);
        testTrace4.setReq_param(objectMapper.writeValueAsString(dto));

        // todo
        testTrace4.setError(0);
        testTrace4.setY_issue_ms("");
        testTrace4.setY_issue_dim_type("");
        testTrace4.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace4);


        HttpHeaders headers = new HttpHeaders();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), adminUpdateOrderTraceId));

        ResponseEntity<UpdateOrderResultDto> addOrderResultEntity = adminUpdateOrder(dto, headers);
        UpdateOrderResultDto upDateOrderResult = addOrderResultEntity.getBody();

        return upDateOrderResult;
    }

    private AddOrderResultDto testAdminAddOrder(AddOrderRequestDto addOrderRequestDto) throws Exception {
        String adminAddOrderTraceId = UUIDUtil.generateUUID();


        TestTrace testTrace3 = new TestTrace();
        testTrace3.setEntryApi("/adminorder/addOrder");
        testTrace3.setEntryService("ts-admin-order-service");
        testTrace3.setEntryTimestamp(System.currentTimeMillis());
        testTrace3.setExpected_result(0);
        testTrace3.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace3.setTestClass("AdminOrderFlowClass");
        testTrace3.setTestMethod("adminAddOrder");
        testTrace3.setTestTraceId(adminAddOrderTraceId);
        testTrace3.setReq_param(objectMapper.writeValueAsString(addOrderRequestDto));

        // todo
        testTrace3.setError(0);
        testTrace3.setY_issue_ms("");
        testTrace3.setY_issue_dim_type("");
        testTrace3.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace3);


        HttpHeaders headers = new HttpHeaders();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), adminAddOrderTraceId));


        ResponseEntity<AddOrderResultDto> addOrderResultEntity = adminAddOrder(addOrderRequestDto, headers);
        AddOrderResultDto allOrdersList = addOrderResultEntity.getBody();

        return allOrdersList;
    }


    private GetAllOrderResultDto testGetAllOrdersResult(String adminId) throws Exception {
        String getAllOrdersTraceId = UUIDUtil.generateUUID();


        TestTrace testTrace2 = new TestTrace();
        testTrace2.setEntryApi("/adminorder/findAll");
        testTrace2.setEntryService("ts-admin-order-service");
        testTrace2.setEntryTimestamp(System.currentTimeMillis());
        testTrace2.setExpected_result(0);
        testTrace2.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace2.setTestClass("AdminOrderFlowClass");
        testTrace2.setTestMethod("getAllOrderResult");
        testTrace2.setTestTraceId(getAllOrdersTraceId);
        testTrace2.setReq_param(objectMapper.writeValueAsString(adminId));

        // todo
        testTrace2.setError(0);
        testTrace2.setY_issue_ms("");
        testTrace2.setY_issue_dim_type("");
        testTrace2.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace2);

        HttpHeaders headers = new HttpHeaders();
        headers.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), getAllOrdersTraceId));

        ResponseEntity<GetAllOrderResultDto> allOrdersResultEntity = getAllOrderResult(adminId, headers);
        GetAllOrderResultDto allOrdersList = allOrdersResultEntity.getBody();

        return allOrdersList;
    }

    /**
     * test admin login
     *
     * @param adminLoginInfoDto
     * @return
     */
    private Contact testAdminLogin(AdminLoginInfoDto adminLoginInfoDto) throws Exception {
        String loginTraceId = UUIDUtil.generateUUID();

        TestTrace testTrace = new TestTrace();
        testTrace.setEntryApi("/account/adminlogin");
        testTrace.setEntryService("ts-sso-service");
        testTrace.setEntryTimestamp(System.currentTimeMillis());
        testTrace.setExpected_result(0);
        testTrace.setTestCaseId(ThreadLocalCache.testCaseIdThreadLocal.get());
        testTrace.setTestClass("BookingFlowTestClass");
        testTrace.setTestMethod("adminLogin");
        testTrace.setTestTraceId(loginTraceId);
        testTrace.setReq_param(objectMapper.writeValueAsString(adminLoginInfoDto));

        // todo
        //ts-login-service
        testTrace.setError(0);
        testTrace.setY_issue_ms("");
        testTrace.setY_issue_dim_type("");
        testTrace.setY_issue_dim_content("");

        ThreadLocalCache.testTracesThreadLocal.get().add(testTrace);


        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.put(ServiceConstant.USER_AGENT, Arrays.asList(ThreadLocalCache.testCaseIdThreadLocal.get(), loginTraceId));
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Contact> loginResponseDtoResp = adminLogin(adminLoginInfoDto, loginHeaders);
        Contact contact = loginResponseDtoResp.getBody();

        return contact;
    }


    /**
     * save test_trace test_case to mysql
     *
     * @param testTraceList
     * @param testCase
     * @return
     */
    public FlowTestResult returnFlowTestResult(List<TestTrace> testTraceList, TestCase testCase) {
        testCaseRepository.save(testCase);
        testTraceRepository.saveAll(testTraceList);
        FlowTestResult ftr = new FlowTestResult();
        ftr.setTestCase(testCase);
        ftr.setTestTraces(testTraceList);
        return ftr;
    }


}
