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
package com.performizeit.jmxsupport;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXConnection {

    String host;
    String port;
    String userName = "";
    String userPassword = "";
    JMXServiceURL serviceURL;
    private final String connectURL;
    private boolean originalThreadContentionEnabledValue;

    public boolean isOriginalThreadContentionEnabledValue() {
        return originalThreadContentionEnabledValue;
    }

    public void setOriginalThreadContentionEnabledValue(boolean originalThreadContentionEnabledValue) {
        this.originalThreadContentionEnabledValue = originalThreadContentionEnabledValue;
    }



    public JMXConnection(String serverUrl, String passwd) throws MalformedURLException {

        int colonIndex = serverUrl.lastIndexOf(":");
        int atIndex = serverUrl.indexOf("@");
        if (atIndex != -1) {
            userName = serverUrl.substring(0, atIndex);
            userPassword = passwd;
        }
        host = serverUrl.substring(atIndex + 1, colonIndex);
        port = serverUrl.substring(colonIndex + 1);
        connectURL = host + ":" + port;
        //    System.out.println("[" + host + "] [" + port + "] [" + userName + "] [" + userPassword + "]");
        serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
    }
    MBeanServerConnection server = null;

    public String getConnectURL() {
        return connectURL;
    }

    public MBeanServerConnection getServerConnection() throws  IOException {
        if (server == null) {

            Map<String,String[]>  env = new HashMap<>();
            if (!userName.isEmpty()) {
                String[] creds = {userName, userPassword};
                env.put(JMXConnector.CREDENTIALS, creds);
            }
            JMXConnector conn = JMXConnectorFactory.connect(serviceURL, env);
            server = conn.getMBeanServerConnection();
        }
        return server;
    }
    public static ObjectName RUNTIME = null;
    public static ObjectName GC = null;
    public static ObjectName THREADING = null;

    static {
        try {
            RUNTIME = new ObjectName("java.lang:type=Runtime");
            GC = new ObjectName("java.lang:type=GarbageCollector,name=*");
            THREADING = new ObjectName("java.lang:type=Threading");
        } catch (MalformedObjectNameException | NullPointerException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public long getUptime() {
        long l = -1;
        try {

            l = (Long) getServerConnection().getAttribute(JMXConnection.RUNTIME, "Uptime");

        } catch (MBeanException | IOException | ReflectionException | InstanceNotFoundException |
                 AttributeNotFoundException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return l;
    }

    public CompositeData[] getThreads(long[] thIds, int stackTraceEntriesNo) throws Exception {
        String[] signature = {"[J", "int"};
        Object[] params = {thIds, stackTraceEntriesNo};
        return (CompositeData[]) server.invoke(THREADING, "getThreadInfo", params, signature);
    }

    public long[] getThreadsCPU(long[] thIds) throws Exception {

        String[] signature = {"[J"};
        Object[] params = {thIds};
        return (long[]) server.invoke(THREADING, "getThreadCpuTime", params, signature);
    }

    public long getThreadCPU(long thId) throws Exception {

        String[] signature = {"long"};
        Object[] params = {thId};
        return (long) (Long) server.invoke(THREADING, "getThreadCpuTime", params, signature);

    }

    public long[] getThreadsAllocBytes(long[] thIds) throws Exception {

        String[] signature = {"[J"};
        Object[] params = {thIds};
        return (long[]) server.invoke(THREADING, "getThreadAllocatedBytes", params, signature);
    }
    private boolean supportAdvFeatures = true;

    public boolean isJava16_25andAbove() {
        return supportAdvFeatures;
    }

    public void unsetJava16_25andAbove() {
        supportAdvFeatures = false;
    }

}
