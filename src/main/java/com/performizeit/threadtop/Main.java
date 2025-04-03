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
import picocli.CommandLine;

public class Main {

    public static void main(String[] args) throws Exception {
        ThreadTopOptions opts = new ThreadTopOptions();
        CommandLine commandLine = new CommandLine(opts);
        
        try {
            commandLine.parseArgs(args);
            if (commandLine.isUsageHelpRequested()) {
                commandLine.usage(System.out);
                printJmxRemoteHelp();
                return;
            }
            
            if (opts.getConectionStringList() == null || opts.getConectionStringList().isEmpty()) {
                System.out.println("Missing process id or host:port to connect");
                commandLine.usage(System.out);
                printJmxRemoteHelp();
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            commandLine.usage(System.out);
            printJmxRemoteHelp();
            System.exit(1);
        }

        String passwd = opts.getPassword();
        ArrayList<JMXConnection> servers = new ArrayList<>();
        
        for (String hostPortUser : opts.getConectionStringList()) {
            JMXConnection server = new JMXConnection(hostPortUser, passwd);
            servers.add(server);
        }

        int stackTraceEntriesNo = opts.getStackTraceEntriesNo();
        // if stackTrace option is selected, set default stackTraceEntries to 1
        if(opts.getStackTraceEntriesNo() == 0 && opts.isPrintStackTrace()) {
            stackTraceEntriesNo = 1;
        }
        
        ThreadTop calc = new ThreadTop(servers, opts.getTimeToMeasure(), opts.getNum(), opts.getSort(), 
                opts.isMeasureThreadCPU(), opts.isMeasureThreadAlloc(),
                opts.isMeasureThreadContention(), opts.getRegExp(), stackTraceEntriesNo);

        calc.getContendedThreads(opts.getIterations());
    }
    
    private static void printJmxRemoteHelp() {
        System.out.println("""
                
                Threadtop can connect to a process on the same machine with pid
                And via JMX port for a remote process.
                To enable remote JMX add the following command line params:
                -Dcom.sun.management.jmxremote
                -Dcom.sun.management.jmxremote.port=[port]
                -Dcom.sun.management.jmxremote.authenticate=false
                -Dcom.sun.management.jmxremote.ssl=false
                """);
    }
}