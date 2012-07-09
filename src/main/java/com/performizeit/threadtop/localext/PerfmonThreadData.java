/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.performizeit.threadtop.localext;

/**
 *
 * @author yadidh
 */
public class PerfmonThreadData {

    int nid;
    int pid;
    String javaThreadName;
    long userCPUStart;
    long userCPUEnd;
    long systemCPUStart;
    long systemCPUEnd;

    public PerfmonThreadData(int pid, int nid, long userCPU, long systemCPU) {
        this.pid = pid;
        this.nid = nid;
        userCPUStart = userCPU;
        systemCPUStart = systemCPU;
        userCPUEnd = -1;
        systemCPUEnd = -1;
        javaThreadName = "";
    }

    public void setEndTimes(long userCPU, long systemCPU) {
        userCPUEnd = userCPU;
        systemCPUEnd = systemCPU;
    }

    public void setJavaThreadName(String tname) {
        javaThreadName = tname;
    }
}
