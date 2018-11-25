package org.services.test.exception.handler;

import org.services.test.cache.ThreadLocalCache;
import org.services.test.entity.MsMapping;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.exception.SeqFaultException;
import org.services.test.exception.UnknownException;
import org.services.test.service.impl.BookingFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

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

        testTraces.forEach(x -> {
            if (x.getEntryApi().equals(e.getErrorBody().getPath())) {
                x.setError(1);
                x.setY_issue_ms("test");
                x.setY_issue_dim_content("test");
                x.setY_issue_dim_type("seq");
            }
        });

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }

    @ExceptionHandler(UnknownException.class)
    @ResponseBody
    public FlowTestResult handleUnknownException(UnknownException e) {
        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();

        testTraces.forEach(x -> {
            String entryApi = x.getEntryApi();
            if (entryApi.equals(e.getErrorBody().getPath())) {
                x.setError(1);
                x.setY_issue_ms(getServiceNameByEntryApi(entryApi));
                x.setY_issue_dim_content("unknown");
                x.setY_issue_dim_type("seq");
            }
        });

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }

    private String getServiceNameByEntryApi(String entryApi) {
        for (MsMapping m : MsMapping.values()) {
            if (entryApi.equals(m.getApi())) {
                return m.getServiceName();
            }
        }

        return "unknown service";
    }
}
