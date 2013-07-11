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
import com.performizeit.threadtop.localext.format.ColumnFormat;
import com.performizeit.threadtop.localext.format.TableFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import javax.management.Attribute;
import javax.management.openmbean.CompositeData;
import static com.performizeit.jmxsupport.JMXConnection.*;

public class ThreadTop {
    // printed columns
    public static final String TS = "TS";
    public static final String PROCID = "PROCID";
    public static final String TID = "TID";
    public static final String BLOCKED_TIME = "BL";
    public static final String BL_PERCENT = "BL%";
    public static final String BL_Count = "#BL";
    public static final String CPU = "CPU";
    public static final String CPU_PERCENT = "CPU%";
    public static final String ALLOC = "ALLOC";
    public static final String NAME = "Name";
    public static final String STACK_TRACE = "Stack Trace";

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
    int stackTraceEntriesNo=0; // no of thread stack trace entries to print

    /**
     * Ctor
     */
    public ThreadTop(ArrayList<JMXConnection> servers, long timeToMeasure, long numThreads, String sortByStr, boolean measureCPU,
                     boolean measureBytesAllocated, boolean measureContention, String filterThreadRegExp,
                     int stackTraceEntriesNo) throws Exception {
        this.servers = servers;
        this.timeToMeasure = timeToMeasure;
        this.numThreads = numThreads;
        this.measureContention = measureContention;
        this.measureCPU = measureCPU;
        this.measureBytesAllocated = measureBytesAllocated;
        this.filterThreadRegExp = filterThreadRegExp;
        this.sortByStr = sortByStr;
        this.stackTraceEntriesNo = stackTraceEntriesNo;
    }

    private void sortBy() {
        if (!measureCPU && !measureContention && !measureBytesAllocated || sortByStr.toLowerCase().startsWith("n")) {
            sortBy = SortBy.NAME;
        } else if (!measureCPU && !measureBytesAllocated || sortByStr.toLowerCase().contains("d")) {
            sortBy = SortBy.CONTENTION;
        } else if (!measureCPU || sortByStr.toLowerCase().startsWith("a")) {
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
            TableFormat tableFormat = new TableFormat();
            tableFormat.addColumn(new ColumnFormat(TS,"%10s ","%10.3f "));
            tableFormat.addColumn(new ColumnFormat(PROCID,"%-20s "));
            tableFormat.addColumn(new ColumnFormat(TID,"%5s ","%5d "));
            if(measureContention) {
                tableFormat.addColumn(new ColumnFormat(BLOCKED_TIME,sortBy == SortBy.CONTENTION,"%6s ","%6d "));
                tableFormat.addColumn(new ColumnFormat(BL_PERCENT,"%4s ","%5.1f "));
            }
            tableFormat.addColumn(new ColumnFormat(BL_Count,"%5s ", "%4d "));
            if(measureCPU) {
                tableFormat.addColumn(new ColumnFormat(CPU,sortBy == SortBy.CPU,"%8s ","%8d "));
                tableFormat.addColumn(new ColumnFormat(CPU_PERCENT,sortBy == SortBy.CPU,"%5s ","%5.1f "));
            }
            if(measureBytesAllocated) {
                tableFormat.addColumn(new ColumnFormat(ALLOC,sortBy == SortBy.ALLOC_BYTES,"%8s ","%8d "));
            }
            tableFormat.addColumn(new ColumnFormat(NAME,sortBy == SortBy.NAME ,"%-50s "));

            if(stackTraceEntriesNo >0) {
                tableFormat.addColumn(new ColumnFormat(STACK_TRACE, "%-50s "));
            }
            if (nSamp == 0) {
                tableFormat.printHeader();
            }
            tableFormat.clean();

            for (int i = 0; i < ((diffThreadInfoArr.length > numThreads) ? numThreads : diffThreadInfoArr.length); i++) {
                tableFormat.format(TS,0.0);   //todo ask Haim
                tableFormat.format(PROCID,diffThreadInfoArr[i].getProcConnect());
                tableFormat.format(TID, diffThreadInfoArr[i].getId());
                tableFormat.format(BLOCKED_TIME, diffThreadInfoArr[i].getBlockedTime());
                tableFormat.format(BL_PERCENT,(100.0 * diffThreadInfoArr[i].getBlockedTime() / timeToMeasure));
                tableFormat.format(BL_Count, diffThreadInfoArr[i].getBlockedCount());
                tableFormat.format(CPU, diffThreadInfoArr[i].getCpuTime() / 1000 / 1000);
                tableFormat.format(CPU_PERCENT, (100.0 * diffThreadInfoArr[i].getCpuTime() / 1000 / 1000 / timeToMeasure));
                tableFormat.format(ALLOC, diffThreadInfoArr[i].getAllocBytes());
                tableFormat.format(NAME, diffThreadInfoArr[i].getName());
                if(stackTraceEntriesNo > 0) {
                    String[] stackTraceEntries = diffThreadInfoArr[i].getStackTrace();
                    for(int j=0; j<stackTraceEntries.length; j++ ) {
                        // for the second stack trace entry shift to the stack trace column
                        if(j>0) {
                            int columnNums = tableFormat.getColumnNo();
                            tableFormat.formatEmptyLine(columnNums-1);
                        }
                        tableFormat.format(STACK_TRACE, stackTraceEntries[j]);
                        // do not print the last row
                        if(j != stackTraceEntries.length-1) {
                            tableFormat.printRaw();
                        }
                    }
                }
                tableFormat.printRaw();
            }
    }

    if (measureContention

    
        ) {
            revertContentionMonitoring();
    }
}
    private void buildThreadInfo(JMXConnection server,HashMap<MyThreadInfo,MyThreadInfo> threadMap) throws Exception {
        long[] thIds = getThreadIds(server);
        CompositeData[] threadsBaseData = server.getThreads( thIds, stackTraceEntriesNo);
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
                        MyThreadInfo mtis = threadMap.get(MyThreadInfo.createProto(server.getConnectURL(),filteredThreadIdsArr[i]) );
                        mtis.setAllocBytes(threadAllocBytes[i]);
                    }

                }
            } catch (Exception e) {
                measureBytesAllocated = false;
                server.unsetJava16_25andAbove();
                if (sortByStr.equals("ALLOC")) {
                    sortByStr = "";
                }
                System.err.println("JVM do not support advanced APIs using slower APIs" );
                e.printStackTrace();
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
