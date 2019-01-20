package org.services.test.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.services.test.config.ClusterConfig;
import org.services.test.entity.constants.ServiceConstant;
import org.services.test.entity.dto.BasicMessage;
import org.services.test.entity.dto.CollectRequestDto;
import org.services.test.entity.dto.ConfirmRequestDto;
import org.services.test.entity.dto.ConfirmResponseDto;
import org.services.test.entity.dto.Contact;
import org.services.test.entity.dto.ExcuteRequestDto;
import org.services.test.entity.dto.FoodRequestDto;
import org.services.test.entity.dto.FoodResponseDto;
import org.services.test.entity.dto.LoginRequestDto;
import org.services.test.entity.dto.LoginResponseDto;
import org.services.test.entity.dto.PaymentRequestDto;
import org.services.test.entity.dto.QueryTicketRequestDto;
import org.services.test.entity.dto.QueryTicketResponseDto;
import org.services.test.service.BookingFlowService;
import org.services.test.util.HeaderUtil;
import org.services.test.util.ParamUtil;
import org.services.test.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class BookingFlowServiceImpl implements BookingFlowService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClusterConfig clusterConfig;

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
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "QueryTravelInfo");
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
            ret = restTemplate.exchange(url, HttpMethod.POST, req,
                    new ParameterizedTypeReference<List<QueryTicketResponseDto>>() {
                    });
        } catch (Exception e) {
        }

        return ret;
    }

    @Override
    public ResponseEntity<List<Contact>> getContacts(Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "GetContact");
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
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "GetFood");
        HttpEntity<FoodRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/food/getFood");
        ResponseEntity<FoodResponseDto> ret = restTemplate.exchange(url, HttpMethod.POST, req, FoodResponseDto.class);

        return ret;
    }

    @Override
    public ResponseEntity<ConfirmResponseDto> preserve(ConfirmRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "PreserveTicket");
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
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "Pay");
        HttpEntity<PaymentRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/inside_payment/pay");
        ResponseEntity<Boolean> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                Boolean.class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> collect(CollectRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "Collect");
        HttpEntity<CollectRequestDto> req = new HttpEntity<>(dto, httpHeaders);

        String url = UrlUtil.constructUrl(clusterConfig.getHost(), clusterConfig.getPort(), "/execute/collected");
        ResponseEntity<BasicMessage> ret = restTemplate.exchange(url, HttpMethod.POST, req,
                BasicMessage.class);
        return ret;
    }

    @Override
    public ResponseEntity<BasicMessage> enter(ExcuteRequestDto dto, Map<String, List<String>> headers) {
        HttpHeaders httpHeaders = HeaderUtil.setHeader(headers);
        httpHeaders.set(HeaderUtil.REQUEST_TYPE_HEADER, "Execute");
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
    public void bookFlow() throws Exception {
        int t1 = 0;
        int t2 = 0;
        int t3 = 0;
        int t4 = 0;
        for (int i = 0; i < 500; i++) {
            log.info(String.format("This is the %d times test", i));
            /******************
             * 1st step: login
             *****************/
            log.info("1st step: login");
            LoginRequestDto loginRequestDto = ParamUtil.constructLoginRequestDtoBySequence(i);

            LoginResponseDto loginResponseDto = testLogin(loginRequestDto);

            // set headers
            // login service will set 2 cookies: login and loginToken, this is mandatory for some other service
            Map<String, List<String>> headers = loginResponseDto.getHeaders();

            /***************************
             * 2nd step: query ticket
             ***************************/
            log.info("2nd step: query ticket");
            QueryTicketRequestDto queryTicketRequestDto = ParamUtil.constructQueryTicketReqDto();
            List<QueryTicketResponseDto> queryTicketResponseDtos = testQueryTicket(headers, queryTicketRequestDto);


            /*************************************
             * 3rd step: get contacts
             *************************************/
            log.info("3rd step: get contacts");
            List<Contact> contacts = testQueryContact(headers);


            /***********************
             * 4th step: get food
             ***********************/
            log.info("4th step: get food");
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
            log.info("5th step: confirm ticket");
            String contactId = ParamUtil.getRandomContact(contacts);// random param
            ConfirmRequestDto confirmRequestDto = ParamUtil.constructConfirmRequestDto(departureTime, startingStation,
                    endingStation, tripId, contactId);
            ConfirmResponseDto confirmResponseDto = testPreserveTicket(headers, confirmRequestDto);
            if (null == confirmResponseDto || null == confirmResponseDto.getOrder()) {
                log.info("Confirm ticket error!");
                return;
            }


            // get random number in [0, 4)
            int randomNumber = new Random().nextInt(4);
            // int randomNumber = 0;
            System.out.println("===================randomNumber: " + randomNumber);

            switch (randomNumber) {
                case 0: { // don't execute step 6, 7 and 8
                    t1++;
                    break;
                }
                case 1: { // execute step 6
                    t2++;
                    /*********************
                     * 6th step: payment
                     *********************/
                    log.info("6th step: payment");
                    String orderId = confirmResponseDto.getOrder().getId().toString();
                    PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
                    testTicketPayment(headers, paymentRequestDto);
                    break;
                }
                case 2: { // execute step 6 and step 7
                    t3++;
                    /*********************
                     * 6th step: payment
                     *********************/
                    log.info("6th step: payment");
                    String orderId = confirmResponseDto.getOrder().getId().toString();
                    PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
                    testTicketPayment(headers, paymentRequestDto);

                    /*****************************
                     * 7th step: collect ticket
                     *****************************/
                    log.info("7th step: collect ticket");
                    CollectRequestDto collectRequestDto = ParamUtil.constructCollectRequestDto(orderId);
                    testTicketCollection(headers, collectRequestDto);
                    break;
                }
                case 3: { // execute step 6, 7 and 8
                    t4++;
                    /*********************
                     * 6th step: payment
                     *********************/
                    log.info("6th step: payment");
                    String orderId = confirmResponseDto.getOrder().getId().toString();
                    PaymentRequestDto paymentRequestDto = ParamUtil.constructPaymentRequestDto(tripId, orderId);
                    testTicketPayment(headers, paymentRequestDto);

                    /*****************************
                     * 7th step: collect ticket
                     *****************************/
                    log.info("7th step: collect ticket");
                    CollectRequestDto collectRequestDto = ParamUtil.constructCollectRequestDto(orderId);
                    testTicketCollection(headers, collectRequestDto);

                    /****************************
                     * 8th step: enter station
                     ****************************/
                    log.info("8th step: enter station");
                    ExcuteRequestDto excuteRequestDto = ParamUtil.constructExecuteRequestDto(orderId);
                    testEnterStation(headers, excuteRequestDto);
                    break;
                }
                default:
                    break;
            }
            Thread.sleep(1000);
            log.info(String.format("%n"));
        }
        log.info(String.format("%n"));
        log.info(String.format("%d times don't include payment", t1));
        log.info(String.format("%d times include payment", t2));
        log.info(String.format("%d times include payment, collect", t3));
        log.info(String.format("%d times include payment, collect, enter", t4));
    }


    private void testEnterStation(Map<String, List<String>> headers, ExcuteRequestDto excuteRequestDto) {
        ResponseEntity<BasicMessage> enterBasicMsgResp = enter(excuteRequestDto, headers);
        BasicMessage enterBasicMsg = enterBasicMsgResp.getBody();
    }

    private void testTicketCollection(Map<String, List<String>> headers, CollectRequestDto collectRequestDto) {
        ResponseEntity<BasicMessage> collectBasicMsgResp = collect(collectRequestDto, headers);
        BasicMessage collectBasicMsg = collectBasicMsgResp.getBody();
    }

    private void testTicketPayment(Map<String, List<String>> headers, PaymentRequestDto paymentRequestDto) {
        pay(paymentRequestDto, headers);
    }

    private ConfirmResponseDto testPreserveTicket(Map<String, List<String>> headers, ConfirmRequestDto
            confirmRequestDto) {
        ResponseEntity<ConfirmResponseDto> confirmResponseDtoResp = preserve(confirmRequestDto, headers);
        return confirmResponseDtoResp.getBody();
    }

    private void testQueryFood(Map<String, List<String>> headers, FoodRequestDto foodRequestDto) {
        getFood(foodRequestDto, headers);
    }

    private List<Contact> testQueryContact(Map<String, List<String>> headers) throws Exception {
        ResponseEntity<List<Contact>> contactsResp = getContacts(headers);
        return contactsResp.getBody();
    }

    private List<QueryTicketResponseDto> testQueryTicket(Map<String, List<String>> headers, QueryTicketRequestDto
            queryTicketRequestDto) {
        ResponseEntity<List<QueryTicketResponseDto>> queryTicketResponseDtosResp = queryTicket(queryTicketRequestDto, headers);
        return queryTicketResponseDtosResp.getBody();
    }

    protected LoginResponseDto testLogin(LoginRequestDto loginRequestDto) throws Exception {
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(ServiceConstant.COOKIE, "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        loginHeaders.add(HeaderUtil.REQUEST_TYPE_HEADER, "Login");
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<LoginResponseDto> loginResponseDtoResp = login(loginRequestDto, loginHeaders);
        return loginResponseDtoResp.getBody();
    }

}
