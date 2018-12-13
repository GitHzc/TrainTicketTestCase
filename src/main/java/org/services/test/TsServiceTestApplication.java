package org.services.test;

import org.services.test.entity.dto.Contact;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SpringBootApplication
public class TsServiceTestApplication {
    // global access time
    public static ConcurrentMap<String, Integer> bookingFlowClientAccessTimeMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, Integer> cancelFlowClientAccessTimeMap = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, Integer> flowThreeClientAccessTimeMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        initBookingFlowAccessTime();
        initCancelFlowAccessTime();
        flowThreeClientAccessTimeMap();
        SpringApplication.run(TsServiceTestApplication.class, args);
    }

    public static void initBookingFlowAccessTime() {
        bookingFlowClientAccessTimeMap.put("food_getFood", 0);
        bookingFlowClientAccessTimeMap.put("preserve", 0);
        // bookingFlowClientAccessTimeMap.put("inside_payment_pay", 0);
        bookingFlowClientAccessTimeMap.put("execute_collected", 0);
        bookingFlowClientAccessTimeMap.put("execute_execute", 0);
        bookingFlowClientAccessTimeMap.put("travel_query", 0);
        bookingFlowClientAccessTimeMap.put("travel2_query", 0);
        bookingFlowClientAccessTimeMap.put("contacts_findContacts", 0);
        bookingFlowClientAccessTimeMap.put("login", 0);
    }

    public static void initCancelFlowAccessTime() {
        cancelFlowClientAccessTimeMap.put("login", 0);
        cancelFlowClientAccessTimeMap.put("order_query", 0);
        cancelFlowClientAccessTimeMap.put("order_other_query", 0);
        cancelFlowClientAccessTimeMap.put("cancel_Calculate_refund", 0);
        cancelFlowClientAccessTimeMap.put("cancel_Order_flow", 0);
    }

    public static void flowThreeClientAccessTimeMap() {
        flowThreeClientAccessTimeMap.put("login", 0);
        flowThreeClientAccessTimeMap.put("query_order", 0);
        flowThreeClientAccessTimeMap.put("query_other_order",0);
        flowThreeClientAccessTimeMap.put("insert_consign", 0);
    }
}
