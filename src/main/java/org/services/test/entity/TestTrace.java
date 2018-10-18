package org.services.test.entity;

public class TestTrace {
    private String testTraceId;
    private String testCaseId;
    private String entryService;
    private String entryApi;
    private String entryTimestamp;
    private String result;
    private String testClass;
    private String testMethod;
    private String error;
    private String y_issue_ms;
    private String y_issue_dimen;

    public String getTestTraceId() {
        return testTraceId;
    }

    @Override
    public String toString() {
        return testTraceId + "," + testCaseId + "," + entryService + "," + entryApi + ","
                + entryTimestamp + "," + result + "," + testClass + "," + testMethod + ","
                + error + "," + y_issue_ms + "," + y_issue_dimen + ";";
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

    public String getEntryTimestamp() {
        return entryTimestamp;
    }

    public void setEntryTimestamp(String entryTimestamp) {
        this.entryTimestamp = entryTimestamp;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getY_issue_ms() {
        return y_issue_ms;
    }

    public void setY_issue_ms(String y_issue_ms) {
        this.y_issue_ms = y_issue_ms;
    }

    public String getY_issue_dimen() {
        return y_issue_dimen;
    }

    public void setY_issue_dimen(String y_issue_dimen) {
        this.y_issue_dimen = y_issue_dimen;
    }
}
