package org.services.test.util;

import org.services.test.entity.dto.BasicMessage;

import java.util.List;

public class AssertUtil {
    public static int assertByStatusCode(int statusCode) {
        if (statusCode < 300 && statusCode > 199) {
            return 0;
        }
        return 1;
    }

    public static boolean assertSeqError(BasicMessage bm){
        if (!bm.isStatus() || bm.getMessage().equals("[Error Process Seq]")){
            return false;
        }
        return true;
    }

    public static int assertByStatus(boolean status) {
        if (status) {
            return 0;
        }
        return 1;
    }

    public static int assertByListSize(int listSize) {
        if (listSize == 0) {
            return 1;
        }
        return 0;
    }
}
