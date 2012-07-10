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
import uk.co.flamingpenguin.jewel.cli.CommandLineInterface;
import uk.co.flamingpenguin.jewel.cli.Option;
import uk.co.flamingpenguin.jewel.cli.Unparsed;

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

    @Option(defaultValue = "1", shortName = "n", description = "Number of top threads to show[default:1]")
    long getNum();

    @Option(shortName = "c")
    boolean isMeasureThreadCPU();

    @Option(shortName = "d")
    boolean isMeasureThreadContention();
    @Option(shortName = "w")
    boolean isAddAllThreadsAndWindowsPerfMonData();    

    @Option(shortName = "a")
    boolean isMeasureThreadAlloc();

    @Option(defaultValue = "CPU", shortName = "s", description = "Sort by CPU/CONTEND/ALLOC/NAME [default:CPU]")
    String getSort();


    @Option(defaultToNull = true, shortName = "u", description = "Set user for remote connect [optional]")
    String getUser();

    @Option(defaultToNull = true, shortName = "p", description = "Set password for remote connect [optional]")
    String getPassword();



    @Option(defaultValue = ".*", shortName = "r", description = "Thread name regex filter [default:.*]")
    public String getRegExp();
        @Unparsed()
    List<String> getConectionStringList();
}
