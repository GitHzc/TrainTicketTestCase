package org.services.test.entity;

public class TestCase {
    private String testCaseId;
    private String sessionId;
    private String userId;
    private String userType;
    private String userDetail;

    public String getTestCaseId() {
        return testCaseId;
    }

    @Override
    public String toString() {
        return testCaseId + "," + sessionId + "," + userId + ","
                + userType + "," + userDetail + ";";
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(String userDetail) {
        this.userDetail = userDetail;
    }
}
