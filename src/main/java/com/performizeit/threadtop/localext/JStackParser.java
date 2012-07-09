/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop.localext;

import static com.performizeit.threadtop.localext.OSUtil.*;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author yadidh
 */
public class JStackParser {

    public static String createJstack(int pid) throws IOException, InterruptedException {
        return executeCommandAndExtractStdOut("jstack -l " + pid);
    }

    public static void addJavaExtra(String allSt) {
        String[] splitSt = splitToThreads(allSt);
        for (String stackTrace : splitSt) {
            Pattern p = Pattern.compile("^(.*)\".*prio=(\\d*).* tid=0x([0-9a-f]*) nid=0x([0-9a-f]*) .*");
            Matcher m = p.matcher(stackTrace);

            if (m.find()) {
                String threadName = m.group(1);
                int prio = Integer.parseInt(m.group(2));
                long jtid = Long.parseLong(m.group(3), 16);
                int nid = Integer.parseInt(m.group(4), 16);
                PerfmonThreadData pmd = ThreadNamesAndIdsOnly.threads.get(nid);
                if (pmd != null) {
                    pmd.setJavaThreadName(threadName);
                } else {
                    System.out.println("Did not find " + nid);
                }
            }
        }
    }

    static String[] splitToThreads(String allStackTraces) {
        String[] splitTraces = allStackTraces.split("\n\n\"");
        return splitTraces;

    }
}
