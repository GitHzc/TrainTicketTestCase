package org.services.test.exception.handler;

import com.fasterxml.jackson.core.JsonParseException;
import org.services.test.cache.ThreadLocalCache;
import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;
import org.services.test.entity.dto.FlowTestResult;
import org.services.test.entity.enums.MsMapping;
import org.services.test.exception.SeqFaultException;
import org.services.test.exception.UnknownException;
import org.services.test.service.impl.BookingFlowServiceImpl;
import org.services.test.util.CollectionUtil;
import org.services.test.util.K8sUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private BookingFlowServiceImpl bookingFlowServiceImpl;

    private static String imageInfo = null;

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static {
        try {
            imageInfo = K8sUtil.getK8sImageByService("ts-execute-service");
            logger.info(imageInfo);
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
            e.printStackTrace();
        }
    }


    @ExceptionHandler(SeqFaultException.class)
    @ResponseBody
    public FlowTestResult handleSeqFaultException(SeqFaultException e) {

        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();
        TestTrace lastTestTrace = CollectionUtil.getLastElement(testTraces);

        lastTestTrace.setExpected_result(1);
        lastTestTrace.setError(1);
        lastTestTrace.setY_issue_dim_type("seq");

        switch (lastTestTrace.getEntryApi()) {
            case "/preserveOther":
                lastTestTrace.setY_issue_ms("ts-preserve-other-service");
                lastTestTrace.setY_issue_dim_content("ts-contacts-service_ts-security-service=0," +
                        "ts-contacts-service_ts-security-service_caller=ts-preserve-other-service");
                break;
            case "/preserve":
                lastTestTrace.setY_issue_ms("ts-preserve-service");
                lastTestTrace.setY_issue_dim_content("ts-contacts-service_ts-security-service=0," +
                        "ts-contacts-service_ts-security-service_caller=ts-preserve-service");
                break;
            case "/cancelOrder":
                lastTestTrace.setY_issue_ms("ts-cancel-service");
                if (ThreadLocalCache.cancelOrderType.get().equals("cancelOrder")) {
                    lastTestTrace.setY_issue_dim_content("ts-inside-payment-service_ts-order-service=0," +
                            "ts-inside-payment-service_ts-order-service_caller=ts-cancel-service");
                } else {
                    lastTestTrace.setY_issue_dim_content("ts-inside-payment-service_ts-order-other-service=0," +
                            "ts-inside-payment-service_ts-order-other-service=ts-cancel-service");
                }
                break;
            case "/execute/execute":
                lastTestTrace.setY_issue_ms("ts-execute-service");
                if (ThreadLocalCache.executeOrderType.get().equals("order")) {
                    lastTestTrace.setY_issue_dim_content("ts-order-service_ts-order-service=0," +
                            "ts-order-service_ts-order-service_caller=ts-execute-service");
                } else {
                    if (imageInfo.contains("1.1") || imageInfo.contains("1.3")) {
                        return null;
                    } else {
                        lastTestTrace.setY_issue_dim_content("ts-order-other-service_ts-order-other-service=0," +
                                "ts-order-other-service_ts-order-other-service=ts-execute-service");
                    }
                    break;

                }
            case "/execute/collected":
                lastTestTrace.setY_issue_ms("ts-execute-service");
                if (ThreadLocalCache.executeOrderType.get().equals("order")) {
                    lastTestTrace.setY_issue_dim_content("ts-order-service_ts-order-service=0," +
                            "ts-order-service_ts-order-service_caller=ts-execute-service");
                } else {
                    if (imageInfo.contains("1.1") || imageInfo.contains("1.3")) {
                        return null;
                    } else {
                        lastTestTrace.setY_issue_dim_content("ts-order-other-service_ts-order-other-service=0," +
                                "ts-order-other-service_ts-order-other-service=ts-execute-service");
                    }
                    break;
                }
                break;
        }

        FlowTestResult flowTestResult = new FlowTestResult();
        flowTestResult.setTestCase(testCase);
        flowTestResult.setTestTraces(testTraces);

        bookingFlowServiceImpl.persistTestData(testCase, testTraces);
        return flowTestResult;
    }


    private void setYissueDim(TestTrace lastTestTrace) throws IOException, NoSuchAlgorithmException,
            KeyStoreException, KeyManagementException {

    }

    @ExceptionHandler(UnknownException.class)
    @ResponseBody
    public FlowTestResult handleUnknownException(UnknownException e) {
        TestCase testCase = ThreadLocalCache.testCaseThreadLocal.get();
        List<TestTrace> testTraces = ThreadLocalCache.testTracesThreadLocal.get();
        TestTrace lastTestTrace = CollectionUtil.getLastElement(testTraces);

        lastTestTrace.setError(1);
        lastTestTrace.setY_issue_dim_type("unknown");
        lastTestTrace.setY_issue_ms("unknown");
        lastTestTrace.setY_issue_dim_content("unknown");

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
