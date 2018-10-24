package org.services.test.service;

import org.services.test.entity.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CancelFlowService {
    ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpHeaders httpHeaders);

    ResponseEntity<List<Order>> queryOrder(OrderQueryRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<List<Order>> queryOrderOther(OrderQueryRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<RefundResponseDto> calculateRefund(RefundRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<BasicMessage> cancelOrder(CancelOrderRequestDto dto, Map<String, List<String>> headers);

    FlowTestResult cancelFlow();
}
