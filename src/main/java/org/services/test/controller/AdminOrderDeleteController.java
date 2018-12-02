package org.services.test.controller;

import org.services.test.entity.dto.*;
import org.services.test.service.AdminOrderFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminOrderDeleteController {


    @Autowired
    private AdminOrderFlowService adminService;

    @GetMapping("/listAndDeleteOrder")
    public FlowTestResult listAndDeleteOrder() throws Exception {
        return adminService.adminOrderFlow();
    }

    @GetMapping("/login")
    public Contact testLogin() {
        AdminLoginInfoDto adminLoginRequestDto = new AdminLoginInfoDto();
        adminLoginRequestDto.setName("adminroot");
        adminLoginRequestDto.setPassword("adminroot");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Contact> contactResponseEntity = null;
        try {
            contactResponseEntity =adminService.adminLogin(adminLoginRequestDto, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactResponseEntity.getBody();
    }


}
