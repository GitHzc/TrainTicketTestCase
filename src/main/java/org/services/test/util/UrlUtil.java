package org.services.test.util;

public class UrlUtil {
    public static String constructUrl(String host, String port, String uri){
        return "http://"+host+":"+port+uri;
    }
}
