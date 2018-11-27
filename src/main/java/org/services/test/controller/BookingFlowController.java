package org.services.test.controller;

import org.services.test.entity.dto.FlowTestResult;
import org.services.test.entity.dto.LoginRequestDto;
import org.services.test.entity.dto.LoginResponseDto;
import org.services.test.service.BookingFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class BookingFlowController {
    @Autowired
    private BookingFlowService bookingFlowService;

    @GetMapping("/bookingflow")
    public FlowTestResult booking() throws Exception {
        return bookingFlowService.bookFlow();
    }

    @GetMapping("/login")
    public LoginResponseDto testLogin() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("fdse_microservices@163.com");
        loginRequestDto.setPassword("DefaultPassword");
        loginRequestDto.setVerificationCode("abcd");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "YsbCaptcha=C480E98E3B734C438EC07CD4EB72AB21");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return bookingFlowService.login(loginRequestDto, headers).getBody();
    }

//    @GetMapping("/ticket/query")
//    public List<QueryTicketResponseDto> testQuery() {
//        return bookingFlowService.queryTicket();
//    }
//
//    @GetMapping("/contacts")
//    public List<Contact> testContacts() {
//        return bookingFlowService.getContacts();
//    }
//
//    @GetMapping("/food")
//    public FoodResponseDto getFood() {
//        return bookingFlowService.getFood();
//    }
//
//    @GetMapping("/preserve")
//    public ConfirmResponseDto preserve() {
//        return bookingFlowService.preservce();
//    }
//
//    @GetMapping("/pay")
//    public boolean pay() {
//        return bookingFlowService.pay();
//    }
//
//    @GetMapping("/collect")
//    public BasicMessage collect() {
//        return bookingFlowService.collect();
//    }
//
//    @GetMapping("/enter")
//    public BasicMessage enter() {
//        return bookingFlowService.enter();
//    }
}
