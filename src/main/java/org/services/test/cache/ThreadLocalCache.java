package org.services.test.cache;

import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ThreadLocalCache {
    private static final Logger logger = LoggerFactory.getLogger(ThreadLocalCache.class);

    public static final ThreadLocal<String> testCaseIdThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<List<TestTrace>> testTracesThreadLocal = new ThreadLocal<>();
    public static final ThreadLocal<TestCase> testCaseThreadLocal = new ThreadLocal<>();

    public static void threadLocalClean() {
        testCaseIdThreadLocal.remove();
        testTracesThreadLocal.remove();
        testCaseThreadLocal.remove();
        logger.info("clean ThreadLocal");
    }
}
