package org.services.test.controller;

import org.services.test.TsServiceTestApplication;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.entity.dto.InitCacheDataResponse;
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
    public FlowTestResult consignFlow() throws Exception {
        return flowThreeService.consignFlow();
    }

    @GetMapping("/initFlowThreeClientCacheData")
    public InitCacheDataResponse initBookingFlowCacheData() {
        TsServiceTestApplication.initFlowThreeClientAccessTimeMap();
        InitCacheDataResponse initCacheDataResponse = new InitCacheDataResponse(true, "init cache data success");
        return initCacheDataResponse;
    }

    @GetMapping("/voucherFlow")
    public FlowTestResult voucherFlow() throws Exception {
        return flowThreeService.voucherFlow();
    }

}
