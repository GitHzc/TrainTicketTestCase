package org.services.test.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVUtil {
    public static void writeToCSV(String path, String rawData) {
        File csv = new File(path);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(csv, true));
            bw.write(rawData);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
