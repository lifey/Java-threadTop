/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop.localext;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author yadidh
 */
public class PerfmonParser {
    public static String createPerfomnOutput(int pid) throws IOException, InterruptedException {
    return OSUtil.executeCommandAndExtractStdOut("cscript //NoLogo threadsOfProcess.vbs " + pid);
    }
        public  static void buildThreadStarts(String perfmonOutput) {
        for (String thread : perfmonOutput.split("\n")) {
            Pattern p = Pattern.compile("^(\\d*),(\\d*),(\\d*),(\\d*)");
            Matcher m = p.matcher(thread);

            if (m.find()) {
                int pid = Integer.parseInt(m.group(1));
                int nid = Integer.parseInt(m.group(2));
                long utime = Long.parseLong(m.group(3));
                long ktime = Long.parseLong(m.group(4));
                PerfmonThreadData pmd = new PerfmonThreadData(pid, nid, utime, ktime);
                ThreadNamesAndIdsOnly.threads.put(nid, pmd);
            }

        }
    }

    public  static void buildThreadEnds(String perfmonOutput) {
        for (String thread : perfmonOutput.split("\n")) {
            Pattern p = Pattern.compile("^(\\d*),(\\d*),(\\d*),(\\d*)");
            Matcher m = p.matcher(thread);

            if (m.find()) {
                int pid = Integer.parseInt(m.group(1));
                int nid = Integer.parseInt(m.group(2));
                long utime = Long.parseLong(m.group(3));
                long ktime = Long.parseLong(m.group(4));
                PerfmonThreadData pmd = ThreadNamesAndIdsOnly.threads.get(nid);
                if (pmd != null) {
                    pmd.setEndTimes(utime, ktime);
                } else {
                    pmd = new PerfmonThreadData(pid, nid, -1, -1);
                    pmd.setEndTimes(utime, ktime);
                    ThreadNamesAndIdsOnly.threads.put(nid, pmd);
                }

            }

        }
    }

    
}
