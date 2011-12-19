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

import javax.management.MBeanServerConnection;

public class Main {

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println("Synopsis: [username@]<host>:<port> [password] [timeForMeasure=5000] [Num top threads=1] [numIter=1] [measureCpu=true] [measureAllocatedBytes(J6u25 and above)=true] [measure contention=true][sort=CPU/ALLOC/CONTEND/NAME] [Thread-name-filter-regex=.*]");

            System.out.println("Enable remote JMX:\n   -Dcom.sun.management.jmxremote\n   -Dcom.sun.management.jmxremote.port=29601\n   -Dcom.sun.management.jmxremote.authenticate=false\n   -Dcom.sun.management.jmxremote.ssl=false\n   -Xloggc:gc.log");
            System.exit(1);
        }
        String hostPortUser = args[0];
        String passwd = args.length > 1 ? args[1] : "";
        JMXConnection con;
        try {
            Integer.parseInt(hostPortUser);
            con = new JMXConnection(hostPortUser);
        } catch (NumberFormatException e) {
            con = new JMXConnection(hostPortUser, passwd);
        }
        int addIdx = 0;
        if (con.isUseAuthentication()) {
            addIdx = 1;
        }
        int timeToMeasure = 5000;
        int numThreads = 1;
        int numIter = 1;
        boolean measureThreadCPU = true;
                boolean measureBytesAllocated = true;
        boolean measureContention = true;
        String sortByStr = "";
        String threadRegexp = ".*";
        if (args.length >= (2 + addIdx)) {
            timeToMeasure = Integer.parseInt(args[1 + addIdx]);
        }
        if (args.length >= (3 + addIdx)) {
            numThreads = Integer.parseInt(args[2 + addIdx]);
        }
        if (args.length >= (4 + addIdx)) {
            numIter = Integer.parseInt(args[3 + addIdx]);
        }
        if (args.length >= (5 + addIdx)) {
            measureThreadCPU = args[4 + addIdx].equals("true");
        }
         if (args.length >= (6 + addIdx)) {
            measureBytesAllocated = args[5 + addIdx].equals("true");
        }
        if (args.length >= (7 + addIdx)) {
            measureContention = args[6 + addIdx].equals("true");
        }
        if (args.length >= (8 + addIdx)) {
            sortByStr = args[7 + addIdx];
        }
        if (args.length >= (9 + addIdx)) {
            threadRegexp = args[8 + addIdx];
        }
        MBeanServerConnection server = con.connectToServer();
        ThreadTop calc = new ThreadTop(server, timeToMeasure, numThreads, sortByStr, measureThreadCPU,measureBytesAllocated, measureContention,threadRegexp);

        calc.getContendedThreads(numIter);
    }
}
