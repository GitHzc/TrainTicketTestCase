package org.services.test.entity.enums;

public enum MsMapping {
    LOGIN("/login", "ts-login-service"),
    TRAVEL("/travel/query", "ts-travel-service"),
    TRAVEL2("/travel2/query", "ts-travel2-service"),
    CONTACT("/contacts/findContacts", "ts-contacts-service"),
    FOOD("/food/getFood", "ts-food-service"),
    PRESERVE("/preserve", "ts-preserve-service"),
    PAYMENT("/inside_payment/pay", "ts-inside-payment-service"),
    COLLECTION("/execute/collected", "ts-execute-service"),
    ENTER("/execute/execute", "ts-execute-service"),

    ORDER_QUERY("/order/query", "ts-order-service"),
    ORDER_OTHER_QUERY("/orderOther/query", "ts-order-other-service"),
    CANCEL_REFUND("/cancelCalculateRefund", "ts-cancel-service"),
    CANCEL("/cancelCalculateRefund", "ts-cancel-service"),

    STATION_QUERY("/station/queryById", "ts-station-service"),
    CONSIGN_INSERT("/consign/insertConsign", "ts-consign-service"),
    CONSIGN_QUERY_BY_ACCOUNT("/consign/findByAccountId", "ts-consign-service"),
    VOUCHER("/voucher.html", "ts-voucher-service"),
    VOUCHER_QUERY("/getVoucher", "ts-voucher-service");

    private String api;
    private String serviceName;

    MsMapping(String api, String serviceName) {
        this.api = api;
        this.serviceName = serviceName;
    }

    public String getApi() {
        return api;
    }

    public String getServiceName() {
        return serviceName;
    }
}
