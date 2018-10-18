package org.services.test.controller;

import org.services.test.entity.dto.VoucherResponseDto;
import org.services.test.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoucherController {
    @Autowired
    private VoucherService testCaseVoucherService;

    @GetMapping("/test")
    public VoucherResponseDto getVoucherServiceTestCase() {

        return testCaseVoucherService.getVoucherInfo();
    }

}
