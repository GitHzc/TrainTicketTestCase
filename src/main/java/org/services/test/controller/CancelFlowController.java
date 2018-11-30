package org.services.test.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.entity.dto.BasicMessage;
import org.services.test.entity.dto.CancelOrderRequestDto;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.entity.dto.YissueDimDto;
import org.services.test.service.CancelFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class CancelFlowController {

    @Autowired
    private CancelFlowService cancelFlowService;

    @PostMapping("/cancelflow")
    public FlowTestResult cancelFlow(@RequestBody YissueDimDto dto) throws JsonProcessingException {
        ThreadLocalCache.yIssueDimDto.set(dto);
        return cancelFlowService.cancelFlow();
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
