/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop;

import picocli.CommandLine;

/**
 *
 * @author yadidh
 */
public class M2 {

    public static void main(String[] args) {
        ThreadTopOptions opts = new ThreadTopOptions();
        CommandLine commandLine = new CommandLine(opts);
        
        try {
            commandLine.parseArgs(args);
            
            System.out.println(opts.getTimeToMeasure());
            System.out.println(opts.isMeasureThreadCPU());
            System.out.println(opts.isMeasureThreadContention());
            System.out.println(opts.isMeasureThreadAlloc());
            System.out.println(opts.getSort());
            System.out.println(opts.isPrintStackTrace());
            System.out.println(opts.getStackTraceEntriesNo());
            
            commandLine.usage(System.out);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            commandLine.usage(System.out);
        }
    }
}
