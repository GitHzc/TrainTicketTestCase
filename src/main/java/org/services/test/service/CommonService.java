package org.services.test.service;

import org.services.test.entity.dto.AddContactsResult;
import org.services.test.entity.dto.QueryTicketRequestDto;
import org.services.test.entity.dto.QueryTicketResponseDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CommonService {
    ResponseEntity<List<QueryTicketResponseDto>> commonQueryTicket(QueryTicketRequestDto dto, Map<String,
            List<String>> headers);

    ResponseEntity<AddContactsResult> commonCreateContact(Map<String, List<String>> headers);
}
