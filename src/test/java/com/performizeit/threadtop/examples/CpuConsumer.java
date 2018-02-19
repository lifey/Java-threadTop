/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop.examples;

/**
 *
 * @author lifey
 */
public class CpuConsumer extends Sleeper{

    int CPUpercent;

    public CpuConsumer(int CPUpercent) {
        this(CPUpercent, 100);// 100 ms interval
    }

    public CpuConsumer(int CPUpercent, long interval) {
        super(interval);
        this.CPUpercent = CPUpercent;
        if (CPUpercent > 100 || CPUpercent < 0) {
            throw new RuntimeException("CPU percent should be between 0..100");
        }
    }

    @Override
    public long singleCycle(long counter) {
        long sleepT = interval * (100 - CPUpercent) / 100;
        long workT = interval * (CPUpercent) / 100;
        counter = work(workT, counter);
        rest(sleepT);
        return counter;

    }



    public long work(long ms, long counter) {
        long start = System.currentTimeMillis();
        do {
            for (int i = 0; i < 1000; i++) {
                counter += i;
            }
        } while (System.currentTimeMillis() - start < ms);
        return counter;
    }

}
