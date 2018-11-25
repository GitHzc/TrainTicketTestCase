package org.services.test.entity;

public enum MsMapping {
    LOGIN("/login", "ts-login-service"),
    TRAVEL("/travel/query", "ts-travel-service"),
    TRAVEL2("/travel2/query", "ts-travel2-service"),
    CONTACT("/contacts/findContacts", "ts-contacts-service"),
    FOOD("/food/getFood", "ts-food-service"),
    PRESERVE("/preserve", "ts-preserve-service"),
    PAYMENT("/inside_payment/pay", "ts-inside-payment-service"),
    COLLECTION("/execute/collected", "ts-execute-service"),
    ENTER("/execute/execute", "ts-execute-service");

    private String api;
    private String serviceName;

    MsMapping(String api, String serviceName) {
        this.api = api;
        this.serviceName = serviceName;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
