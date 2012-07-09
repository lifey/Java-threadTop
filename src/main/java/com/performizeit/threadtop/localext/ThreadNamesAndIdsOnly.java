/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop.localext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static com.performizeit.threadtop.localext.OSUtil.*;

/**
 *
 * @author yadidh
 */
public class ThreadNamesAndIdsOnly {

    static HashMap<Integer, PerfmonThreadData> threads = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        if (args.length < 2) {
            System.out.println("Synopsis <pid> <interval> \nor\n <threaddump> <perfmonvbsdump1> <perfmonvbsdump2>");
        }
        String firstParam = args[0];
        int pid = -1;
        try {
            pid = Integer.parseInt(firstParam);
        } catch (NumberFormatException e) {
        }
        String allSt = null;
        String perfmonOutput1 = null;
        String perfmonOutput2 = null;
        if (pid == -1) {
            allSt = readTextFile(firstParam);
            perfmonOutput1 = readTextFile(args[1]);
            perfmonOutput2 = readTextFile(args[2]);
        } else {
            allSt = JStackParser.createJstack(pid);
            perfmonOutput1 = PerfmonParser.createPerfomnOutput(pid);

            // System.out.println(perfmonOutput1);
            Thread.sleep(Integer.parseInt(args[1]));
            perfmonOutput2 = PerfmonParser.createPerfomnOutput(pid);
        }
        PerfmonParser.buildThreadStarts(perfmonOutput1);
        PerfmonParser.buildThreadEnds(perfmonOutput2);
        JStackParser.addJavaExtra(allSt);
        for (Integer nid : threads.keySet()) {
            PerfmonThreadData pmd = threads.get(nid);
            long userDiff = -1;
            long kernelDiff = -1;
            if (pmd.userCPUEnd != -1 && pmd.userCPUStart != -1) {
                userDiff = pmd.userCPUEnd - pmd.userCPUStart;
            }
            if (pmd.systemCPUEnd != -1 && pmd.systemCPUStart != -1) {
                kernelDiff = pmd.systemCPUEnd - pmd.systemCPUStart;
            }
            System.out.println(pmd.javaThreadName + "," + nid + "," + userDiff + "," + kernelDiff);
        }


    }


}
