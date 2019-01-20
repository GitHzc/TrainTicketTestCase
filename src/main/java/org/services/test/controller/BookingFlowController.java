package org.services.test.controller;

import org.services.test.entity.dto.LoginRequestDto;
import org.services.test.entity.dto.LoginResponseDto;
import org.services.test.service.BookingFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class BookingFlowController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookingFlowService bookingFlowService;

    @GetMapping("/bookingflow")
    public void booking() throws Exception {
        log.info("Receive booking flow request");
        bookingFlowService.bookFlow();
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
}
