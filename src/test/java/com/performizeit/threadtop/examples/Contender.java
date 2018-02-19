package com.performizeit.threadtop.examples;


public class Contender extends Sleeper {

    int contendInterval;
    final Object lock;

    public Contender(Object lock,int contendInterval) {
        this(lock,contendInterval, 100);// 100 ms interval
    }

    public Contender(Object lock,int contendInterval, long interval ) {
        super( interval);
        this.lock = lock;
        this.contendInterval = contendInterval;
        if (contendInterval > interval || contendInterval < 0) {
            throw new RuntimeException("contendInterval should be between 0..interval");
        }

    }

    @Override
    public long singleCycle(long counter) {
        long sleepT = interval-contendInterval;
        long workT = contendInterval;
         contend(workT );
        rest(sleepT);
        return counter;

    }
 
    public void contend(long ms) {
        synchronized (lock) {
            rest(ms);
        }
    }
}
