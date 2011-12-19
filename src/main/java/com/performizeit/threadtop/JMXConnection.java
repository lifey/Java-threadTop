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

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import java.io.File;
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
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JMXConnection {

    String host;
    String port;
    String userName ="";
    String userPassword="";
    JMXServiceURL serviceURL;
    private static final String CONNECTOR_ADDRESS =
            "com.sun.management.jmxremote.localConnectorAddress";

    public JMXConnection(String pid) throws AttachNotSupportedException, IOException, AgentLoadException, AgentInitializationException {
        // attach to the target application
        com.sun.tools.attach.VirtualMachine vm =
                com.sun.tools.attach.VirtualMachine.attach(pid.toString());
        JMXServiceURL u;
        try {
            // get the connector address
            String connectorAddress =
                    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

            // no connector address, so we start the JMX agent
            if (connectorAddress == null) {
                String agent = vm.getSystemProperties().getProperty("java.home")
                        + File.separator + "lib" + File.separator
                        + "management-agent.jar";
                vm.loadAgent(agent);

                // agent is started, get the connector address
                connectorAddress =
                        vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            }

            // establish connection to connector server
           // System.out.println(connectorAddress);
            serviceURL = new JMXServiceURL(connectorAddress);

        } finally {
            vm.detach();
        }
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
        System.out.println("[" + host + "] [" + port + "] [" + userName + "] [" + userPassword + "]");
        serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
    }

    public MBeanServerConnection connectToServer() throws MalformedURLException, IOException {

        Map env = new HashMap();
        if (userName.length() > 0) {
            String[] creds = {userName, userPassword};
            env.put(JMXConnector.CREDENTIALS, creds);
        }
        JMXConnector conn = JMXConnectorFactory.connect(serviceURL, env);
        MBeanServerConnection server = conn.getMBeanServerConnection();
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
        } catch (MalformedObjectNameException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static long getUptime(MBeanServerConnection server) {
        long l = -1;
        try {

            l = (Long) server.getAttribute(JMXConnection.RUNTIME, "Uptime");

        } catch (MBeanException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AttributeNotFoundException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstanceNotFoundException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ReflectionException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return l;
    }

    public static float inSecsTimestamp(long ts) {
        return ((float) ts) / 1000;

    }

    boolean isUseAuthentication() {
        return !userName.isEmpty();
    }
}
