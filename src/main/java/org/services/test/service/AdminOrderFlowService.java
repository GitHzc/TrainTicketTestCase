package org.services.test.service;

import org.services.test.entity.dto.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import javax.xml.ws.Response;
import java.util.List;
import java.util.Map;

public interface AdminOrderFlowService {

    ResponseEntity<Contact> adminLogin(AdminLoginInfoDto dto, HttpHeaders httpHeaders) throws Exception;
    ResponseEntity<GetAllOrderResultDto> getAllOrderResult( String adminId , Map<String, List<String>> headers);
    ResponseEntity<AddOrderResultDto> adminAddOrder(AddOrderRequestDto dto, Map<String, List<String>> headers);
    ResponseEntity<UpdateOrderResultDto> adminUpdateOrder(UpdateOrderRequestDto dto, Map<String, List<String>> headers);
    ResponseEntity<DeleteOrderResultDto> adminDeleteOrder(DeleteOrderRequestDto dto, Map<String, List<String>> headers);

    ResponseEntity<GetRoutesListlResultDto> getAllRoutesResult( String adminId , Map<String, List<String>> headers);
    ResponseEntity<FindAllTravelResultDto> getAllTravelResult( String adminId , Map<String, List<String>> headers);

    FlowTestResult adminOrderFlow() throws Exception;


}
