package org.services.test.service;

import org.services.test.entity.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface AdminService {

    ResponseEntity<Contact> adminLogin(AdminLoginInfoDto dto, HttpHeaders httpHeaders) throws Exception;
    ResponseEntity<GetAllOrderResultDto> getAllOrderResult( String adminId , Map<String, List<String>> headers);
    FlowTestResult adminOrderFlow() ;


}
