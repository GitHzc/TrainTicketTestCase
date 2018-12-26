package org.services.test.exception.handler;

import org.services.test.cache.ThreadLocalCache;
import org.services.test.entity.enums.MsMappingEnum;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.exception.ConfigFaultException;
import org.services.test.exception.SeqFaultException;
import org.services.test.exception.UnknownException;
import org.services.test.service.impl.BookingFlowServiceImpl;
import org.services.test.util.TestTraceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.SocketTimeoutException;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private BookingFlowServiceImpl bookingFlowServiceImpl;

    @ExceptionHandler(SeqFaultException.class)
    @ResponseBody
    public FlowTestResult handleSeqFaultException(SeqFaultException e) {

        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();

        testTraces.get(testTraces.size() - 1).setExpected_result(1);
        testTraces.get(testTraces.size() - 1).setError(1);
        testTraces.get(testTraces.size() - 1).setY_issue_dim_type("Sequence");
        testTraces.get(testTraces.size() - 1).setY_issue_dim_content("test");
        testTraces.get(testTraces.size() - 1).setY_issue_ms(MsMappingEnum.getServiceNameByApi(testTraces.get(testTraces.size() - 1).getEntryApi()));

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }

    @ExceptionHandler({UnknownException.class})
    @ResponseBody
    public FlowTestResult handleUnknownException(UnknownException e) {
        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();

        testTraces.get(testTraces.size() - 1).setExpected_result(0);
        testTraces.get(testTraces.size() - 1).setError(1);
        testTraces.get(testTraces.size() - 1).setY_issue_dim_type("Unknown");
        testTraces.get(testTraces.size() - 1).setY_issue_dim_content("Unknown");
        testTraces.get(testTraces.size() - 1).setY_issue_ms(MsMappingEnum.getServiceNameByApi(testTraces.get(testTraces.size() - 1).getEntryApi()));

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }

    @ExceptionHandler({ConfigFaultException.class, SocketTimeoutException.class})
    @ResponseBody
    public FlowTestResult handleConfigFaultException(ConfigFaultException e) {
        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();

        testTraces.get(testTraces.size() - 1).setExpected_result(1);
        testTraces.get(testTraces.size() - 1).setError(1);
        testTraces.get(testTraces.size() - 1).setY_issue_dim_type("Config");
        testTraces.get(testTraces.size() - 1).setY_issue_dim_content(TestTraceUtil.checkErrorType(e.getMessage()));
        testTraces.get(testTraces.size() - 1).setY_issue_ms(MsMappingEnum.getServiceNameByApi(testTraces.get(testTraces.size() - 1).getEntryApi()));

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }
}
