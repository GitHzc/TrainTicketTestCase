package org.services.test.controller;

import org.services.test.entity.dto.LoginResponseDto;
import org.services.test.service.BookingFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingFlowController {
    @Autowired
    private BookingFlowService bookingFlowService;

    @GetMapping("/logintest")
    public LoginResponseDto get(){
        return bookingFlowService.login();
    }
}
