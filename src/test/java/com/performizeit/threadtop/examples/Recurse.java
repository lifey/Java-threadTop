package com.performizeit.threadtop.examples;


public class Recurse extends Sleeper {

    int depth;
    Runnable r;

    public Recurse(int depth, Runnable r) {
        this.depth = depth;
        this.r = r;
    }
    @Override
    public long singleCycle(long counter) {
        doRecursion(depth);
        rest(interval);
        return counter;

    }
    void doRecursion(int  d ) {
        if (d <= 0) {
            r.run();
        } else {
            doRecursion(d - 1);
        }
    }
}
