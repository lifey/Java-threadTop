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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
    String processId = null;
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
        connectURL = serverUrl;

        // Check if this is a process ID (local process)
        if (serverUrl.matches("\\d+")) {
            processId = serverUrl;
            return;
        }

        // Otherwise, parse as host:port
        int colonIndex = serverUrl.lastIndexOf(":");
        int atIndex = serverUrl.indexOf("@");
        if (atIndex != -1) {
            userName = serverUrl.substring(0, atIndex);
            userPassword = passwd;
        }
        host = serverUrl.substring(atIndex + 1, colonIndex);
        port = serverUrl.substring(colonIndex + 1);
        
        serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
    }
    
    MBeanServerConnection server = null;

    public String getConnectURL() {
        return connectURL;
    }

    public MBeanServerConnection getServerConnection() throws IOException {
        if (server == null) {
            if (processId != null) {
                // Use local connection via attach API for process ID
                try {
                    server = getLocalJvmConnection(processId);
                    if (server == null) {
                        throw new IOException("Could not connect to local JVM with PID: " + processId);
                    }
                } catch (Exception e) {
                    throw new IOException("Error connecting to local JVM: " + e.getMessage(), e);
                }
            } else {
                // Use remote connection
                Map<String, String[]> env = new HashMap<>();
                if (!userName.isEmpty()) {
                    String[] creds = {userName, userPassword};
                    env.put(JMXConnector.CREDENTIALS, creds);
                }
                JMXConnector conn = JMXConnectorFactory.connect(serviceURL, env);
                server = conn.getMBeanServerConnection();
            }
        }
        return server;
    }

    /**
     * Connects to a local JVM process using the Attach API (jdk.attach module)
     * This replaces the old tools.jar approach for Java 9+
     */
    private MBeanServerConnection getLocalJvmConnection(String pid) {
        try {
            // Dynamically load classes from jdk.attach module
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            
            // Get the attach method and connect to the target JVM
            Method attachMethod = vmClass.getDeclaredMethod("attach", String.class);
            Object vm = attachMethod.invoke(null, pid);
            
            try {
                // Get local connector address or start management agent if needed
                Method getAgentPropsMethod = vmClass.getDeclaredMethod("getAgentProperties");
                Properties agentProps = (Properties) getAgentPropsMethod.invoke(vm);
                
                String connectorAddress = (String) agentProps.get("com.sun.management.jmxremote.localConnectorAddress");
                
                // If JMX is not enabled, try to start the management agent
                if (connectorAddress == null) {
                    try {
                        // First try startLocalManagementAgent method (Java 9+)
                        Logger.getLogger(JMXConnection.class.getName()).log(Level.INFO, 
                                "Starting local management agent for JVM process " + pid);
                        Method startLocalManagementAgent = vmClass.getDeclaredMethod("startLocalManagementAgent");
                        startLocalManagementAgent.invoke(vm);
                    } catch (NoSuchMethodException nsme) {
                        // Fall back to loading agent explicitly
                        Logger.getLogger(JMXConnection.class.getName()).log(Level.INFO, 
                                "Loading management agent for JVM process " + pid);
                        
                        Method getSystemPropsMethod = vmClass.getDeclaredMethod("getSystemProperties");
                        Properties sysProps = (Properties) getSystemPropsMethod.invoke(vm);
                        String javaHome = sysProps.getProperty("java.home");
                        
                        // This is the method that loads the agent JAR
                        Method loadAgentMethod = vmClass.getDeclaredMethod("loadAgent", String.class);
                        
                        // Try multiple possible locations for the management agent
                        String[] agentPaths = {
                            javaHome + "/lib/management-agent.jar",
                            javaHome + "/../lib/management-agent.jar",
                            javaHome + "/jmods/jdk.management.agent.jmod",
                            javaHome + "/jre/lib/management-agent.jar"
                        };
                        
                        boolean success = false;
                        for (String path : agentPaths) {
                            try {
                                Logger.getLogger(JMXConnection.class.getName()).log(Level.INFO, 
                                        "Trying to load management agent from: " + path);
                                loadAgentMethod.invoke(vm, path);
                                success = true;
                                break;
                            } catch (InvocationTargetException e) {
                                // Log the error but continue trying the next path
                                Logger.getLogger(JMXConnection.class.getName()).log(Level.WARNING, 
                                        "Failed to load management agent from " + path + ": " + e.getCause().getMessage());
                            }
                        }
                        
                        if (!success) {
                            throw new IOException("Could not load management agent from any location");
                        }
                    }
                    
                    // Get connector address again after loading the agent
                    agentProps = (Properties) getAgentPropsMethod.invoke(vm);
                    connectorAddress = (String) agentProps.get("com.sun.management.jmxremote.localConnectorAddress");
                    
                    if (connectorAddress == null) {
                        throw new IOException("Failed to get JMX connector address after starting management agent");
                    }
                }
                
                // Create JMX connector with the address we got
                JMXServiceURL url = new JMXServiceURL(connectorAddress);
                JMXConnector connector = JMXConnectorFactory.connect(url);
                
                // Detach from the VM (placed here to ensure we detach even if connection fails)
                Method detachMethod = vmClass.getDeclaredMethod("detach");
                detachMethod.invoke(vm);
                
                return connector.getMBeanServerConnection();
                
            } catch (Exception e) {
                // Make sure we detach from the VM even if we failed
                try {
                    Method detachMethod = vmClass.getDeclaredMethod("detach");
                    detachMethod.invoke(vm);
                } catch (Exception detachEx) {
                    // Ignore detach errors
                }
                throw e;
            }
            
        } catch (ClassNotFoundException e) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, 
                    "Could not load Attach API. Make sure jdk.attach module is available.", e);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, 
                    "Error using Attach API", e);
        } catch (InvocationTargetException | IOException e) {
            Logger.getLogger(JMXConnection.class.getName()).log(Level.SEVERE, 
                    "Error connecting to local JVM process", e);
        }
        
        return null;
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