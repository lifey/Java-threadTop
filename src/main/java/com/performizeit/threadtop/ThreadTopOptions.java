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

import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 *
 * @author yadidh
 */
@Command(name = "threadTop", mixinStandardHelpOptions = true, 
         description = "Monitor Java thread CPU, contention, and allocation usage")
public class ThreadTopOptions {

    @Option(names = {"-m"}, defaultValue = "5000", description = "Set amount of time to measure in milliseconds [default: ${DEFAULT-VALUE}]")
    private long timeToMeasure;

    @Option(names = {"-i"}, defaultValue = "1", description = "Number of iterations [default: ${DEFAULT-VALUE}]")
    private int iterations;

    @Option(names = {"-n"}, defaultValue = "10", description = "Number of top threads to show [default: ${DEFAULT-VALUE}]")
    private long num;

    @Option(names = {"-c"}, description = "Measure thread CPU")
    private boolean measureThreadCPU;

    @Option(names = {"-d"}, description = "Measure thread contention")
    private boolean measureThreadContention;
    
    @Option(names = {"-w"}, description = "Add all threads and Windows PerfMon data")
    private boolean addAllThreadsAndWindowsPerfMonData;    

    @Option(names = {"-a"}, description = "Measure thread allocation")
    private boolean measureThreadAlloc;

    @Option(names = {"-s"}, defaultValue = "CPU", description = "Sort by (C)PU/CONTEN(D)/(A)LLOC/(N)AME [default: ${DEFAULT-VALUE}]")
    private String sort;

    @Option(names = {"-u"}, description = "Set user for remote connect [optional]")
    private String user;

    @Option(names = {"-p"}, description = "Set password for remote connect [optional]")
    private String password;

    @Option(names = {"-r"}, defaultValue = ".*", description = "Thread name regex filter [default: ${DEFAULT-VALUE}]")
    private String regExp;
    
    @Parameters(paramLabel = "CONNECT", description = "pid | host:port")
    private List<String> conectionStringList;

    @Option(names = {"-t"}, description = "Grab stack trace for each thread and present thread stack top")
    private boolean printStackTrace;

    @Option(names = {"-x"}, defaultValue = "0", description = "Number of stack trace entries to display [default: ${DEFAULT-VALUE}]")
    private int stackTraceEntriesNo;

    // Getters
    public long getTimeToMeasure() {
        return timeToMeasure;
    }

    public int getIterations() {
        return iterations;
    }

    public long getNum() {
        return num;
    }

    public boolean isMeasureThreadCPU() {
        return measureThreadCPU;
    }

    public boolean isMeasureThreadContention() {
        return measureThreadContention;
    }
    
    public boolean isAddAllThreadsAndWindowsPerfMonData() {
        return addAllThreadsAndWindowsPerfMonData;
    }

    public boolean isMeasureThreadAlloc() {
        return measureThreadAlloc;
    }

    public String getSort() {
        return sort;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getRegExp() {
        return regExp;
    }

    public List<String> getConectionStringList() {
        return conectionStringList;
    }

    public boolean isPrintStackTrace() {
        return printStackTrace;
    }

    public int getStackTraceEntriesNo() {
        return stackTraceEntriesNo;
    }
}
