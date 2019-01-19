package org.services.test.controller;

import org.services.test.service.FlowThreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class FlowThreeController {

    @Autowired
    private FlowThreeService flowThreeService;

    @GetMapping("/consignFlow")
    public void consignFlow() throws Exception {
        flowThreeService.consignFlow();
    }

    @GetMapping("/voucherFlow")
    public void voucherFlow() throws Exception {
        flowThreeService.voucherFlow();
    }
}
