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
    public static ConcurrentMap<String, Integer> clientAccessTimeMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        initAccessTime();
        SpringApplication.run(TsServiceTestApplication.class, args);
    }

    public static void initAccessTime(){
        clientAccessTimeMap.put("food_getFood", 0);
        clientAccessTimeMap.put("preserve", 0);
        clientAccessTimeMap.put("inside_payment_pay", 0);
        clientAccessTimeMap.put("execute_collected", 0);
        clientAccessTimeMap.put("execute_execute", 0);
        clientAccessTimeMap.put("travel_query", 0);
        clientAccessTimeMap.put("travel2_query", 0);
        clientAccessTimeMap.put("contacts_findContacts", 0);
        clientAccessTimeMap.put("login", 0);
    }
}
