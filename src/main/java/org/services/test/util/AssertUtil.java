package org.services.test.util;

public class AssertUtil {
    public static int assertByStatusCode(int statusCode) {
        if (statusCode < 300 && statusCode > 199) {
            return 0;
        }

        return 1;
    }
}
