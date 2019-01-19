package org.services.test.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.dto.*;
import org.services.test.service.FlowThreeService;
import org.services.test.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowThreeServiceImpl implements FlowThreeService {

    @Autowired
    BookingFlowServiceImpl bookingFlowServiceImpl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClusterConfig clusterConfig;

    @Override
    public List<Order> queryOrders(OrderQueryRequestDto orderQueryRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> requestBody = new HttpEntity<>(orderQueryRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/order/query");
        ResponseEntity<List<Order>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, new ParameterizedTypeReference<List<Order>>() {
        });

        return responseEntity.getBody();
    }

    @Override
    public List<Order> queryOrdersOther(OrderQueryRequestDto orderQueryRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<OrderQueryRequestDto> requestBody = new HttpEntity<>(orderQueryRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/orderOther/query");
        ResponseEntity<List<Order>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, new ParameterizedTypeReference<List<Order>>() {
        });

        return responseEntity.getBody();
    }

    @Override
    public StationNameResponseDto queryStationNameById(StationNameRequestDto stationNameRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<StationNameRequestDto> requestBody = new HttpEntity<>(stationNameRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/station/queryById");
        ResponseEntity<StationNameResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, StationNameResponseDto.class);

        return responseEntity.getBody();
    }

    @Override
    public ConsignInsertResponseDto consignOrder(ConsignInsertRequestDto consignInsertRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<ConsignInsertRequestDto> requestBody = new HttpEntity<>(consignInsertRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/consign/insertConsign");
        ResponseEntity<ConsignInsertResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, ConsignInsertResponseDto.class);

        return responseEntity.getBody();
    }

    @Override
    public List<ConsignInsertRequestDto> queryConsignedOrders(String accountId, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/consign/findByAccountId/" + accountId);
        ResponseEntity<List<ConsignInsertRequestDto>> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestBody, new ParameterizedTypeReference<List<ConsignInsertRequestDto>>() {
        });

        return responseEntity.getBody();
    }

    @Override
    public Object getVoucherHtml(VoucherUIRequestDto voucherUIRequestDto, Map<String, List<String>> headers) {

        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity requestBody = new HttpEntity<>(httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/voucher.html");
        String param = "?orderId=" + voucherUIRequestDto.getOrderId() + "&train_number=" + voucherUIRequestDto.getTrain_number();
        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);

        try {
            responseEntity = restTemplate.exchange(url + param, HttpMethod.GET, requestBody, String.class);
        }
        catch (Exception e) {
            if (e.getMessage().contains("JsonParseException")) {
                return responseEntity.getBody();
            }
        }

        return responseEntity.getBody();
    }

    @Override
    public VoucherInfoResponseDto getVoucherInfo(VoucherInfoRequestDto voucherInfoRequestDto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        HttpEntity<VoucherInfoRequestDto> requestBody = new HttpEntity<>(voucherInfoRequestDto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/getVoucher");
        ResponseEntity<VoucherInfoResponseDto> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestBody, new ParameterizedTypeReference<VoucherInfoResponseDto>() {
        });

        return responseEntity.getBody();
    }

    @Override
    public void consignFlow() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();

        /*
         * Get orders, contain steps:
         *     1. login
         *     2. query order service
         *     3. query order service
         */
        List<Order> orders = getOrders(headers);
        if (CollectionUtils.isEmpty(orders)) {
            return ;
        }

        /*
         * Query station name by id, contain steps:
         *     4. query station name by id
         */
        // the logic is redundant in system now, so we get a random item instead.
        Order randomOrder = RandomUtil.getRandomElementInList(orders);
        queryStationNameById(headers, randomOrder.getFrom(), randomOrder.getTo());


        /*
         * 5. consign order
         */
        List<Order> consignableOrders = orders.stream().filter(
                order -> 0 == order.getStatus()
                        || 1 == order.getStatus()
                        || 2 == order.getStatus()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(consignableOrders)) {
            return ;
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
        // the logic is redundant in system now, so we get a random item instead.
        ConsignInsertRequestDto randomConsignOrder = RandomUtil.getRandomElementInList(consignOrders);
        queryStationNameById(headers, randomConsignOrder.getFrom(), randomConsignOrder.getTo());
    }

    @Override
    public void voucherFlow() throws Exception {
        Map<String, List<String>> headers = new HashMap<>();


        /*
         * Get orders, contain steps:
         *     1. login
         *     2. query order service
         *     3. query order other service
         */
        List<Order> orders = getOrders(headers);
        if (CollectionUtils.isEmpty(orders)) {
            return ;
        }


        /*
         * Query station name by id, contain steps:
         *     4. query station name by id
         */
        // the logic is redundant in system now, so we get a random item instead.
        Order randomOrder = RandomUtil.getRandomElementInList(orders);
        queryStationNameById(headers, randomOrder.getFrom(), randomOrder.getTo());


        /*
         * 5. Print voucher, get voucher html
         */
        List<Order> voucherableOrders = orders.stream().filter(
                order -> 6 == order.getStatus()).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(voucherableOrders)) {
            return;
        }

        Order voucherableOrder = RandomUtil.getRandomElementInList(voucherableOrders);
        VoucherUIRequestDto voucherUIRequestDto = ParamUtil.constructVoucherUIRequestDto(voucherableOrder);
        testGetVoucherHtml(headers, voucherUIRequestDto);


        /*
         * 6. get voucher information
         */
        VoucherInfoRequestDto voucherInfoRequestDto = ParamUtil.constructVoucherInfoRequestDto(voucherableOrder);
        testGetVoucherInfo(headers, voucherInfoRequestDto);
    }

    private List<Order> testQueryOrders(Map<String, List<String>> headers, OrderQueryRequestDto orderQueryRequestDto) throws Exception {
        return queryOrders(orderQueryRequestDto, headers);
    }

    private List<Order> testQueryOrdersOther(Map<String, List<String>> headers, OrderQueryRequestDto orderQueryRequestDto) throws Exception {
        return queryOrdersOther(orderQueryRequestDto, headers);
    }

    private void testQueryStationNameById(Map<String, List<String>> headers, StationNameRequestDto stationNameRequestDto) throws Exception {
        queryStationNameById(stationNameRequestDto, headers);
    }

    private void testConsignOrder(Map<String, List<String>> headers, ConsignInsertRequestDto consignInsertRequestDto) throws Exception {
        consignOrder(consignInsertRequestDto, headers);
    }

    private List<ConsignInsertRequestDto> testQueryConsignOrder(Map<String, List<String>> headers, String accountId) throws Exception {
        return queryConsignedOrders(accountId, headers);
    }

    private void testGetVoucherHtml(Map<String, List<String>> headers, VoucherUIRequestDto voucherUIRequestDto) throws Exception {
        getVoucherHtml(voucherUIRequestDto, headers);
    }

    private void testGetVoucherInfo(Map<String, List<String>> headers, VoucherInfoRequestDto voucherInfoRequestDto) throws Exception {
        getVoucherInfo(voucherInfoRequestDto, headers);
    }

    private List<Order> getOrders(Map<String, List<String>> headers) throws Exception {

        /*
         * 1. login
         */
        LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDto();
        LoginResponseDto loginResponseDto = bookingFlowServiceImpl.testLogin(loginRequestDto);

        // set headers
        // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
        headers.putAll(loginResponseDto.getHeaders());

        /*
         * 2. query order service
         */
        OrderQueryRequestDto orderQueryRequestDto = ParamUtil.constructOrderQueryRequestDto();
        List<Order> orders = testQueryOrders(headers, orderQueryRequestDto);


        /*
         * 3. query order other service
         */
        OrderQueryRequestDto orderOtherQueryRequestDto = ParamUtil.constructOrderQueryRequestDto();
        List<Order> ordersOther = testQueryOrdersOther(headers, orderOtherQueryRequestDto);

        List<Order> allOrders = new ArrayList<>();
        allOrders.addAll(orders);
        allOrders.addAll(ordersOther);

        return allOrders;
    }

    private void queryStationNameById(Map<String, List<String>> headers, String from, String to) throws Exception {
        /*
         * 4. query station name by id
         */
        StationNameRequestDto stationNameRequestDto;

        stationNameRequestDto = ParamUtil.constructStationNameRequestDto(from);
        testQueryStationNameById(headers, stationNameRequestDto);

        stationNameRequestDto = ParamUtil.constructStationNameRequestDto(to);
        testQueryStationNameById(headers, stationNameRequestDto);
    }

}
