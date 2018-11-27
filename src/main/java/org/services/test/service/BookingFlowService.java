package org.services.test.service;

import org.services.test.entity.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BookingFlowService {
    ResponseEntity<LoginResponseDto> login(LoginRequestDto dto, HttpHeaders httpHeaders);

    ResponseEntity<List<QueryTicketResponseDto>> queryTicket(QueryTicketRequestDto dto,  Map<String, List<String>> headers);

    ResponseEntity<List<Contact>> getContacts(Map<String, List<String>> headers);

    ResponseEntity<FoodResponseDto> getFood(FoodRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<ConfirmResponseDto> preserve(ConfirmRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<Boolean> pay(PaymentRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<BasicMessage> collect(CollectRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<BasicMessage> enter(ExcuteRequestDto dto, Map<String, List<String>> headers);

    FlowTestResult bookFlow() throws Exception;
}
