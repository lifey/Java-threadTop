/*
 *
 * Copyright 2011 Performize-IT LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.performizeit.threadtop;

import com.performizeit.jmxsupport.JMXConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import javax.management.Attribute;
import javax.management.MBeanServerConnection;
import javax.management.openmbean.CompositeData;
import static com.performizeit.jmxsupport.JMXConnection.*;

public class ThreadTop {

    public enum SortBy {

        NAME,
        CPU,
        CONTENTION,
        ALLOC_BYTES
    }
    private ArrayList<JMXConnection> servers;
    private final long timeToMeasure;
    private final long numThreads;
    SortBy sortBy;
    String sortByStr;
    boolean measureCPU;
    boolean measureBytesAllocated;
    boolean measureContention;
    String filterThreadRegExp;

    public ThreadTop(ArrayList<JMXConnection> servers, long timeToMeasure, long numThreads, String sortByStr, boolean measureCPU, boolean measureBytesAllocated, boolean measureContention, String filterThreadRegExp) throws Exception {
        this.servers = servers;
        this.timeToMeasure = timeToMeasure;
        this.numThreads = numThreads;
        this.measureContention = measureContention;
        this.measureCPU = measureCPU;
        this.measureBytesAllocated = measureBytesAllocated;
        this.filterThreadRegExp = filterThreadRegExp;
        this.sortByStr = sortByStr;
       


    }

    private void sortBy() {
        if (!measureCPU && !measureContention && !measureBytesAllocated || sortByStr.equals("NAME")) {
            sortBy = SortBy.NAME;
        } else if (!measureCPU && !measureBytesAllocated || sortByStr.equals("CONTEND")) {
            sortBy = SortBy.CONTENTION;
        } else if (!measureCPU || sortByStr.equals("ALLOC")) {
            sortBy = SortBy.ALLOC_BYTES;
        } else {
            sortBy = SortBy.CPU;
        }
    }

    private void enableContentionMonitoring() throws Exception {

        Attribute a = new Attribute("ThreadContentionMonitoringEnabled", Boolean.TRUE);
        for (JMXConnection server : servers) {
            boolean origValue = (Boolean) server.getServerConnection().getAttribute(THREADING, "ThreadContentionMonitoringEnabled");
            server.getServerConnection().setAttribute(THREADING, a);
            server.setOriginalThreadContentionEnabledValue(origValue);

        }
    }

    private void revertContentionMonitoring() throws Exception {
        for (JMXConnection server : servers) {
            Attribute a = new Attribute("ThreadContentionMonitoringEnabled", server.isOriginalThreadContentionEnabledValue());
            server.getServerConnection().setAttribute(THREADING, a);
        }
    }

    private long[] getThreadIds(JMXConnection server) throws Exception {

        long[] thIds = (long[]) server.getServerConnection().getAttribute(THREADING, "AllThreadIds");
        return thIds;

    }

    private class BlockedTimeComparator implements Comparator<MyThreadInfo> {

        public int compare(MyThreadInfo o1, MyThreadInfo o2) {
            return o1.getBlockedTime() >= o2.getBlockedTime() ? -1 : 1;
        }
    }

    private class NameComparator implements Comparator<MyThreadInfo> {

        public int compare(MyThreadInfo o1, MyThreadInfo o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    private class CpuTimeComparator implements Comparator<MyThreadInfo> {

        public int compare(MyThreadInfo o1, MyThreadInfo o2) {
            return o1.getCpuTime() >= o2.getCpuTime() ? -1 : 1;
        }
    }

    private class AllocBytesComparator implements Comparator<MyThreadInfo> {

        public int compare(MyThreadInfo o1, MyThreadInfo o2) {
            return o1.getAllocBytes() >= o2.getAllocBytes() ? -1 : 1;
        }
    }

    public void getContendedThreads(int numSamples) throws Exception {

        if (measureContention) {
            enableContentionMonitoring();

        }
        long st = System.currentTimeMillis();
        HashMap <MyThreadInfo, MyThreadInfo> startThreads = new HashMap<MyThreadInfo, MyThreadInfo> ();
        for (JMXConnection server : servers) {
             buildThreadInfo(server,startThreads);
        }
   //     System.out.println("Measuring took " + (System.currentTimeMillis() - st));
        for (int nSamp = 0; nSamp < numSamples; nSamp++) {
            Thread.sleep(timeToMeasure);
             HashMap <MyThreadInfo, MyThreadInfo> endThreads = new HashMap<MyThreadInfo, MyThreadInfo> ();
            for (JMXConnection server : servers) {
                buildThreadInfo(server,endThreads);
            }
            ArrayList<MyThreadInfo> diffThreadInfo = new ArrayList<MyThreadInfo>();
            for (MyThreadInfo mtie : endThreads.values()) {
                MyThreadInfo mtis = startThreads.get(mtie);
                if (mtis != null) {
                    MyThreadInfo diff = new MyThreadInfo(mtis, mtie);
                    diffThreadInfo.add(diff);
                }
            }
            startThreads = endThreads; // start thinking about next iteration 
            

            MyThreadInfo[] diffThreadInfoArr = diffThreadInfo.toArray(new MyThreadInfo[diffThreadInfo.size()]);
            Comparator<MyThreadInfo> comp;
            sortBy();
            if (sortBy == SortBy.CPU) {
                comp = new CpuTimeComparator();

            } else if (sortBy == SortBy.CONTENTION) {
                comp = new BlockedTimeComparator();
            } else if (sortBy == SortBy.ALLOC_BYTES) {
                comp = new AllocBytesComparator();
            } else {
                comp = new NameComparator();
            }
            Arrays.sort(diffThreadInfoArr, comp);
           // float ts = inSecsTimestamp(server.getUptime());
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter(sb, Locale.US);
            if (nSamp == 0) {
                formatter.format("%10s %-20s %5s %6s %4s %5s %8s %5s %8s %-50s", "TS", "PROCID","TID", sortBy == SortBy.CONTENTION ? ">BL" : "BL", "BL%", "#BL", sortBy == SortBy.CPU ? ">CPU" : "CPU", "CPU%", sortBy == SortBy.ALLOC_BYTES ? ">ALLOC" : "ALLOC", sortBy == SortBy.NAME ? ">Name" : "Name");
                System.out.println(formatter.toString());
            }
            sb.setLength(0);

            for (int i = 0; i < ((diffThreadInfoArr.length > numThreads) ? numThreads : diffThreadInfoArr.length); i++) {
                formatter.format("%10.3f %-20s %5d ", 0.0 /*ts*/, diffThreadInfoArr[i].getProcConnect() ,diffThreadInfoArr[i].getId());
                if (measureContention) {
                    formatter.format("%6d %5.1f %4d ", diffThreadInfoArr[i].getBlockedTime(), (100.0 * diffThreadInfoArr[i].getBlockedTime() / timeToMeasure), diffThreadInfoArr[i].getBlockedCount());
                } else {
                    formatter.format("%6s %6s %4d ", "N/A", "N/A", diffThreadInfoArr[i].getBlockedCount());
                }
                if (measureCPU) {
                    formatter.format("%8d %5.1f ", diffThreadInfoArr[i].getCpuTime() / 1000 / 1000, (100.0 * diffThreadInfoArr[i].getCpuTime() / 1000 / 1000 / timeToMeasure));
                } else {
                    formatter.format("%8s %6s ", "N/A", "N/A");

                }
                if (measureBytesAllocated) {
                    formatter.format("%8d ", diffThreadInfoArr[i].getAllocBytes());
                } else {
                    formatter.format("%8s ", "N/A");

                }
                formatter.format("%-50s", diffThreadInfoArr[i].getName());
                System.out.println(formatter.toString());
                sb.setLength(0);
            }
        
    }
    
    if (measureContention

    
        ) {
            revertContentionMonitoring();
    }
}
private void buildThreadInfo(JMXConnection server,HashMap<MyThreadInfo,MyThreadInfo> threadMap) throws Exception {
        long[] thIds = getThreadIds(server);
        CompositeData[] threadsBaseData = server.getThreads( thIds);
        ThreadFilter tf = new ThreadFilterByRegExp(filterThreadRegExp);//".*((RMI)|(JMX)|(ajp)).*"
        ArrayList<Long> filteredThreadIds = new ArrayList<Long>();
         

        for (int i = 0; i < threadsBaseData.length; i++) { // build map
            MyThreadInfo mtis = new MyThreadInfo(server.getConnectURL(),threadsBaseData[i]);
            if (tf.matchFilter(mtis)) {
                threadMap.put( mtis,mtis);
                filteredThreadIds.add(mtis.getId());
            }
        }
        if (server.isJava16_25andAbove()) {
            try {
                long[] filteredThreadIdsArr = new long[filteredThreadIds.size()];
                for (int i = 0; i < filteredThreadIds.size(); i++) {
                    filteredThreadIdsArr[i] = filteredThreadIds.get(i);
                }
                if (measureCPU) {
                    long threadCpus[] = server.getThreadsCPU( filteredThreadIdsArr);
                    for (int i = 0; i < filteredThreadIdsArr.length; i++) {
                        MyThreadInfo mtis = threadMap.get(MyThreadInfo.createProto(server.getConnectURL(),filteredThreadIdsArr[i]) );
                        mtis.setCpuTime(threadCpus[i]);
                    }
                }
                if (measureBytesAllocated) {
                    long threadAllocBytes[] = server.getThreadsAllocBytes( filteredThreadIdsArr);
                    for (int i = 0; i < filteredThreadIdsArr.length; i++) {
                        MyThreadInfo mtis = threadMap.get(filteredThreadIdsArr[i]);
                        mtis.setAllocBytes(threadAllocBytes[i]);
                    }

                }
            } catch (Exception e) {
                measureBytesAllocated = false;
                server.unsetJava16_25andAbove();
                if (sortByStr.equals("ALLOC")) {
                    sortByStr = "";
                }
                System.err.println("JVM do not support advanced APIs using slower APIs");
            }

        }
        if (!server.isJava16_25andAbove()) {
            if (measureCPU) {
                for (int i = 0; i < filteredThreadIds.size(); i++) {
                    MyThreadInfo mtis = threadMap.get(MyThreadInfo.createProto(server.getConnectURL(),filteredThreadIds.get(i)));
                    mtis.setCpuTime(server.getThreadCPU( mtis.getId()));
                }
            }
        }

        
    }
}
