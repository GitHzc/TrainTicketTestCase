package org.services.test.controller;

import org.services.test.entity.dto.FlowTestResult;
import org.services.test.service.CancelFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class CancelFlowController {

    @Autowired
    private CancelFlowService cancelFlowService;

    @GetMapping("/cancelflow")
    public FlowTestResult cancelFlow(){
        return cancelFlowService.cancelFlow();
    }
}
