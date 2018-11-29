package org.services.test.service;

import org.services.test.entity.dto.QueryTicketResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CommonService {
    ResponseEntity<List<QueryTicketResponseDto>> queryTicket(Map<String, List<String>> headers);
}
