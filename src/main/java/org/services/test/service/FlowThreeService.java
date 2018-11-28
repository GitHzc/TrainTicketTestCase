package org.services.test.service;

import org.services.test.entity.dto.*;

import java.util.List;
import java.util.Map;

public interface FlowThreeService {
    List<Order> queryOrders(OrderQueryRequestDto orderQueryRequestDto, Map<String, List<String>> headers);

    FlowTestResult consignFlow() throws Exception;

    FlowTestResult voucherFlow() throws Exception;

    List<Order> queryOrdersOther(OrderQueryRequestDto orderQueryRequestDto, Map<String, List<String>> headers);

    StationNameResponseDto queryStationNameById(StationNameRequestDto stationNameRequestDto, Map<String,
            List<String>> headers);

    ConsignInsertResponseDto consignOrder(ConsignInsertRequestDto consignInsertRequestDto, Map<String, List<String>>
            headers);

    List<ConsignInsertRequestDto> queryConsignedOrders(String accountId, Map<String, List<String>> headers);

    Object getVoucherHtml(VoucherUIRequestDto voucherUIRequestDto, Map<String, List<String>> headers);

    VoucherInfoResponseDto getVoucherInfo(VoucherInfoRequestDto voucherInfoRequestDto, Map<String, List<String>>
            headers);
}