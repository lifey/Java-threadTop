/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop;

import java.util.logging.Level;
import java.util.logging.Logger;
import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

/**
 *
 * @author yadidh
 */
public class M2 {

    public static void main(String args[]) {
        try {
            ThreadTopOptions opts = CliFactory.parseArguments(ThreadTopOptions.class, args);
            System.out.println(opts.getTimeToMeasure());
            System.out.println(opts.isMeasureThreadCPU());
            System.out.println(opts.isMeasureThreadContention());
            System.out.println(opts.isMeasureThreadAlloc());
            System.out.println(opts.getSort());
            System.out.println(CliFactory.createCli(ThreadTopOptions.class).getHelpMessage());
        } catch (ArgumentValidationException ex) {
            System.out.println(ex.getMessage());
            System.out.println(CliFactory.createCli(ThreadTopOptions.class).getHelpMessage());
        }


    }
}
