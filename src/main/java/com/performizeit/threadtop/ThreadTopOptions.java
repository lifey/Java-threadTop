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
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

/**
 *
 * @author yadidh
 */
@CommandLineInterface(application = "threadTop")
public interface ThreadTopOptions {

    @Option(defaultValue = "5000", shortName = "m", description = "Set amount of time to measure in milliseconds [default:5000]")
    long getTimeToMeasure();

    @Option(defaultValue = "1", shortName = "i", description = "Number of iterations [default:1]")
    int getIterations();

    @Option(defaultValue = "10", shortName = "n", description = "Number of top threads to show[default:10]")
    long getNum();

    @Option(shortName = "c")
    boolean isMeasureThreadCPU();

    @Option(shortName = "d")
    boolean isMeasureThreadContention();
    @Option(shortName = "w")
    boolean isAddAllThreadsAndWindowsPerfMonData();    

    @Option(shortName = "a")
    boolean isMeasureThreadAlloc();

    @Option(defaultValue = "CPU", shortName = "s", description = "Sort by (C)PU/CONTEN(D)/(A)LLOC/(N)AME [default:C]")
    String getSort();


    @Option(defaultToNull = true, shortName = "u", description = "Set user for remote connect [optional]")
    String getUser();

    @Option(defaultToNull = true, shortName = "p", description = "Set password for remote connect [optional]")
    String getPassword();

    @Option(defaultValue = ".*", shortName = "r", description = "Thread name regex filter [default:.*]")
    public String getRegExp();
        @Unparsed(description = "pid | host:port ")
    List<String> getConectionStringList();

    @Option(defaultToNull = false, shortName = "t", description = "grab stack trace for each thread and present thread stack top [optional]")
    boolean isPrintStackTrace();

    @Option(defaultValue = "0", shortName = "x", description = "number of stack trace entries to display [default:1]")
    int getStackTraceEntriesNo();

}
