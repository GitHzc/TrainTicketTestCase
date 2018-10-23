package org.services.test.entity;

public class TestTrace {
    private String testTraceId;
    private String testCaseId;
    private String entryService;
    private String entryApi;
    private long entryTimestamp;
    private String testClass;
    private String testMethod;
    private String req_param;
    private int expected_result;
    private int error;
    private String y_issue_ms;
    private String y_issue_dim_type;
    private String y_issue_dim_content;

    @Override
    public String toString() {
        return testTraceId + "," + testCaseId + "," + entryService + "," + entryApi + ","
                + entryTimestamp + "," + testClass + "," + testMethod + "," + req_param + ","
                + expected_result + "," + error + "," + y_issue_ms + ","
                + y_issue_dim_type + "," + y_issue_dim_content;
    }

    public String getReq_param() {
        return req_param;
    }

    public void setReq_param(String req_param) {
        this.req_param = req_param;
    }

    public int getExpected_result() {
        return expected_result;
    }

    public void setExpected_result(int expected_result) {
        this.expected_result = expected_result;
    }

    public String getY_issue_dim_type() {
        return y_issue_dim_type;
    }

    public void setY_issue_dim_type(String y_issue_dim_type) {
        this.y_issue_dim_type = y_issue_dim_type;
    }

    public String getY_issue_dim_content() {
        return y_issue_dim_content;
    }

    public void setY_issue_dim_content(String y_issue_dim_content) {
        this.y_issue_dim_content = y_issue_dim_content;
    }

    public String getTestTraceId() {
        return testTraceId;
    }

    public void setTestTraceId(String testTraceId) {
        this.testTraceId = testTraceId;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getEntryService() {
        return entryService;
    }

    public void setEntryService(String entryService) {
        this.entryService = entryService;
    }

    public String getEntryApi() {
        return entryApi;
    }

    public void setEntryApi(String entryApi) {
        this.entryApi = entryApi;
    }

    public long getEntryTimestamp() {
        return entryTimestamp;
    }

    public void setEntryTimestamp(long entryTimestamp) {
        this.entryTimestamp = entryTimestamp;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getY_issue_ms() {
        return y_issue_ms;
    }

    public void setY_issue_ms(String y_issue_ms) {
        this.y_issue_ms = y_issue_ms;
    }

}
