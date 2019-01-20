package org.services.test.init;

import org.services.test.service.BookingFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RunScript implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private BookingFlowService bookingFlowService;

    @Override
    public void run(String... strings) throws Exception {
        log.info("开始执行初始化命令");
        bookingFlowService.bookFlow();
        log.info("执行初始化命令完成");
    }

}
