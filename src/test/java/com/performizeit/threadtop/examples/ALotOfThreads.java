/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.performizeit.threadtop.examples;

/**
 *
 * @author lifey
 */
public class ALotOfThreads {
    public static void main (String args[]){
        int numIdleThreads = 7;
        int numRunThreads = 7;
        int numContendedThreads = 7;
        final int recursionDepth = 10;
        for (int i =0;i<numIdleThreads;i++) {
           Runnable r = new Recurse(recursionDepth, new Sleeper());
 

           Thread t = new Thread(r);
           t.setName("BEER-"+i);   //idle
           t.start();
        }

        for (int i =0;i<numRunThreads;i++) {
           Runnable r = new Recurse(recursionDepth, new CpuConsumer(5+i,100));
           Thread t = new Thread(r);
           t.setName("COFFEE-"+i);// Consumers
           t.start();
        }
        final Object lock = new Object();
        for (int i =0;i<numContendedThreads;i++) {
           Runnable r = new Recurse(recursionDepth,new Contender(lock,30,100));

           Thread t = new Thread(r);
           t.setName("KAVA-"+i);  // Contender
           t.start();
        }
        (new Sleeper()).run();
    }

}
