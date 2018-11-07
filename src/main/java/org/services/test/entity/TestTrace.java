package org.services.test.entity;

import org.hibernate.annotations.GenericGenerator;
import org.services.test.entity.constants.DBConstants;

import javax.persistence.*;

@Entity
@Table(name = DBConstants.TABLE_TEST_TRACE)
public class TestTrace {

    @Id
    @Column(name = DBConstants.TEST_TRACE_ID)
    @GeneratedValue(generator  = DBConstants.ID_STRATEGY)
    @GenericGenerator(name = DBConstants.ID_STRATEGY, strategy = DBConstants.UUID)
    private String testTraceId;

    @Column(name = DBConstants.TEST_CASE_ID)
    private String testCaseId;

    @Column(name = DBConstants.ENTRY_SERVICE)
    private String entryService;

    @Column(name = DBConstants.ENTRY_API)
    private String entryApi;

    @Column(name = DBConstants.ENTRY_TIME_STAMP)
    private long entryTimestamp;

    @Column(name = DBConstants.TEST_CLASS)
    private String testClass;

    @Column(name = DBConstants.TEST_METHOD)
    private String testMethod;

    @Column(name = DBConstants.REQ_PARAM, columnDefinition = "varchar(5000) COMMENT 'Request Parameter'")
    private String req_param;

    @Column(name = DBConstants.EXPECTED_RESULT)
    private int expected_result;

    @Column(name = DBConstants.ERROR)
    private int error;

    @Column(name = DBConstants.Y_ISSUE_MS)
    private String y_issue_ms;

    @Column(name = DBConstants.USER_TYPE)
    private String y_issue_dim_type;

    @Column(name = DBConstants.Y_ISSUE_DIM_CONTENT, columnDefinition = "varchar(5000) COMMENT 'Issue Dimension Content'")
    private String y_issue_dim_content;

    @Column(name = DBConstants.SEQUENCE)
    private int sequence;

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

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

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
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
