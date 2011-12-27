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

import java.util.ArrayList;
import uk.co.flamingpenguin.jewel.cli.CliFactory;

public class Main {

    public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println(CliFactory.createCli(ThreadTopOptions.class).getHelpMessage());
            System.out.println("Enable remote JMX:\n   -Dcom.sun.management.jmxremote\n   -Dcom.sun.management.jmxremote.port=29601\n   -Dcom.sun.management.jmxremote.authenticate=false\n   -Dcom.sun.management.jmxremote.ssl=false\n   -Xloggc:gc.log");
            System.exit(1);
        }





        ThreadTopOptions opts = CliFactory.parseArguments(ThreadTopOptions.class, args);
                String passwd = opts.getPassword();
         ArrayList<JMXConnection> servers = new ArrayList<JMXConnection>();
        for (String hostPortUser:opts.getConectionStringList()) {


        JMXConnection server;
        try {
            Integer.parseInt(hostPortUser);
            server = new JMXConnection(hostPortUser);
        } catch (NumberFormatException e) {
            server = new JMXConnection(hostPortUser, passwd);
        }
        
        servers.add(server);
        }
        ThreadTop calc = new ThreadTop(servers, opts.getTimeToMeasure(), opts.getNum(), opts.getSort(), opts.isMeasureThreadCPU(), opts.isMeasureThreadAlloc(), opts.isMeasureThreadContention(), opts.getRegExp());

        calc.getContendedThreads(opts.getIterations());
    }
}
