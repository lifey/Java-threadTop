/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.performizeit.threadtop.examples;


public class Sleeper implements Runnable {

    protected long interval;
    protected long deltaRandom;

    public Sleeper() {
        this( 100);// 100 ms interval
    }

    public Sleeper( long interval ) {
        this.interval = interval;
    }

    public long singleCycle(long counter) {
        rest(interval);
        return counter;

    }
    protected long counter=101;

    public void loopForever() {
        while (true) {
            singleCycle(counter);
        }
    }

    public void rest(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
        }
    }

    public void run() {
        loopForever();
    }
}

