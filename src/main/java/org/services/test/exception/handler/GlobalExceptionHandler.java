package org.services.test.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.entity.dto.YissueDimDto;
import org.services.test.entity.enums.MsMapping;
import org.services.test.exception.SeqFaultException;
import org.services.test.exception.UnknownException;
import org.services.test.service.impl.BookingFlowServiceImpl;
import org.services.test.util.CollectionUtil;
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
        TestTrace lastTestTrace = CollectionUtil.getLastElement(testTraces);
        YissueDimDto yissueDimDto = ThreadLocalCache.yIssueDimDto.get();

        lastTestTrace.setExpected_result(1);
        lastTestTrace.setError(1);
        lastTestTrace.setY_issue_dim_type(yissueDimDto.getType());
        lastTestTrace.setY_issue_ms(yissueDimDto.getMs());
        lastTestTrace.setY_issue_dim_content(yissueDimDto.getContent());

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
        TestTrace lastTestTrace = CollectionUtil.getLastElement(testTraces);
        YissueDimDto yissueDimDto = ThreadLocalCache.yIssueDimDto.get();

        lastTestTrace.setError(1);
        lastTestTrace.setY_issue_dim_type(yissueDimDto.getType());
        lastTestTrace.setY_issue_ms(yissueDimDto.getMs());
        lastTestTrace.setY_issue_dim_content(yissueDimDto.getContent());

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }

    @ExceptionHandler(JsonParseException.class)
    @ResponseBody
    public String handleJsonParseException(JsonParseException e) {
        return "json parse error";
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
