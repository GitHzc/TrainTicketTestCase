package org.services.test.entity;

import org.hibernate.annotations.GenericGenerator;
import org.services.test.entity.constants.DBConstants;

import javax.persistence.*;

@Entity
@Table(name = DBConstants.TABLE_TEST_CASE)
public class TestCase {

    @Id()
    @Column(name = DBConstants.TEST_CASE_ID)
    @GeneratedValue(generator  = DBConstants.ID_STRATEGY)
    @GenericGenerator(name = DBConstants.ID_STRATEGY, strategy = DBConstants.UUID)
    private String testCaseId;

    @Column(name = DBConstants.SESSION_ID)
    private String sessionId;

    @Column(name = DBConstants.USER_ID)
    private String userId;

    @Column(name = DBConstants.USER_TYPE)
    private String userType;

    @Column(name = DBConstants.USER_DETAIL)
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
