package org.services.test.controller;

import org.services.test.TsServiceTestApplication;
import org.services.test.entity.dto.BasicMessage;
import org.services.test.entity.dto.CancelOrderRequestDto;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.entity.dto.InitCacheDataResponse;
import org.services.test.service.CancelFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class CancelFlowController {

    @Autowired
    private CancelFlowService cancelFlowService;

    @GetMapping("/cancelflow")
    public FlowTestResult cancelFlow() {
        return cancelFlowService.cancelFlow();
    }


    @GetMapping("/initCancelFlowCacheData")
    public InitCacheDataResponse initBookingFlowCacheData() {
        TsServiceTestApplication.initCancelFlowAccessTime();
        InitCacheDataResponse initCacheDataResponse = new InitCacheDataResponse(true, "init cache data success");
        return initCacheDataResponse;
    }


    @GetMapping("/cancelOrder")
    public ResponseEntity<BasicMessage> cancelOrder() {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("loginId", Arrays.asList("4d2a46c7-71cb-4cf1-b5bb-b68406d9da6f"));
        headers.put("loginToken", Arrays.asList("03579d24-5f07-46ff-8ee9-2b83d5438a92"));

        CancelOrderRequestDto cancelOrderRequestDto = new CancelOrderRequestDto();
        cancelOrderRequestDto.setOrderId("aebee42e-3c27-4f2d-8400-9dd0820dd4e0");
        return cancelFlowService.cancelOrder(cancelOrderRequestDto, headers);
    }
}
