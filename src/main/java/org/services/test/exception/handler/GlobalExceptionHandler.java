package org.services.test.exception.handler;

import org.services.test.cache.ThreadLocalCache;
import org.services.test.entity.enums.MsMapping;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.exception.InstanceFaultException;
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

    @ExceptionHandler(value = UnknownException.class)
    @ResponseBody
    public FlowTestResult errorHandler(UnknownException e) {

        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();

        int size = testTraces.size() - 1;
        String entryApi = testTraces.get(size).getEntryApi();

        testTraces.get(size).setError(1);
        testTraces.get(size).setExpected_result(0);
        testTraces.get(size).setY_issue_ms(getServiceNameByEntryApi(entryApi));
        testTraces.get(size).setY_issue_dim_content(e.getMessage());
        testTraces.get(size).setY_issue_dim_type("unknown");


        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.returnFlowTestResult(testTraces, testCase);
        return flowTestResult;
    }


    @ExceptionHandler(value = InstanceFaultException.class)
    @ResponseBody
    public  FlowTestResult errorHandler(InstanceFaultException e){
        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();

        int size = testTraces.size() - 1;
        String entryApi = testTraces.get(size).getEntryApi();

        testTraces.get(size).setError(1);
        testTraces.get(size).setY_issue_ms(getServiceNameByEntryApi(entryApi));
        testTraces.get(size).setY_issue_dim_content(e.getMessage());
        testTraces.get(size).setY_issue_dim_type("instance");


        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.returnFlowTestResult(testTraces, testCase);
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
