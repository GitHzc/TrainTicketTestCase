package org.services.test.entity.dto;

import org.services.test.entity.TestCase;
import org.services.test.entity.TestTrace;

import java.io.Serializable;
import java.util.List;

public class BookingFlowTestResult implements Serializable{
    private static final long serialVersionUID = 7454458135370741518L;

    private TestCase testCase;
    private List<TestTrace> testTraces;

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public List<TestTrace> getTestTraces() {
        return testTraces;
    }

    public void setTestTraces(List<TestTrace> testTraces) {
        this.testTraces = testTraces;
    }
}
