/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop.localext;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author yadidh
 */
public class OSUtil {

    public static String executeCommandAndExtractStdOut(String cmd) throws IOException, InterruptedException {

        String line;
        Process p = Runtime.getRuntime().exec(cmd);
        BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder allS = new StringBuilder();
        while ((line = bri.readLine()) != null) {
            allS.append(line + "\n");
        }
        bri.close();

        p.waitFor();
        return allS.toString();


    }

    public static String readTextFile(String fn) throws IOException {
        FileInputStream fis = new FileInputStream(fn);
        int numBytes = fis.available();

        byte[] buf = new byte[numBytes];
        fis.read(buf);
        String content = new String(buf);
        return content;

    }
}
