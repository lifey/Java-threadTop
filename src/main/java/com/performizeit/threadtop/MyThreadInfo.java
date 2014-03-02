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

import com.performizeit.threadtop.localext.StackTraceParser;

import javax.management.openmbean.CompositeData;

public class MyThreadInfo {

    protected long id;
    protected String name;
    protected long blockedTime;
    protected long blockedCount;
    protected long cpuTime;
    protected long allocBytes;
    protected String procConnect;
    protected CompositeData[] stackTraceElems;

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MyThreadInfo other = (MyThreadInfo) obj;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 47 * hash + (this.procConnect != null ? this.procConnect.hashCode() : 0);
        return hash;
    }
    protected long toLong(Object  o,long def) {
        if (o==null || !(o instanceof Long)) {
            return def;
        }
        Long l = (Long) o;

        return l.longValue();
    }
    public MyThreadInfo(String procConnect, CompositeData thread) {
        blockedTime = toLong(thread.get("blockedTime"),0);
        blockedCount = toLong(thread.get("blockedCount"), 0);
        name = (String) thread.get("threadName");
        id = toLong(thread.get("threadId"),0);
        stackTraceElems = (CompositeData []) thread.get("stackTrace");
        this.procConnect = procConnect;
    }

    public MyThreadInfo(String procConnect, long threadId) {

        id = threadId;
        this.procConnect = procConnect;
    }

    public MyThreadInfo(MyThreadInfo t1, MyThreadInfo t2) {
        blockedTime = t2.blockedTime - t1.blockedTime;
        blockedCount = t2.blockedCount - t1.blockedCount;
        cpuTime = t2.cpuTime - t1.cpuTime;
        allocBytes = t2.allocBytes - t1.allocBytes;
        name = t1.name;
        id = t1.id;
        procConnect=t1.procConnect;
        stackTraceElems = t2.stackTraceElems; //todo ask Haim
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getBlockedTime() {
        return blockedTime;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public long getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(long cpuTime) {
        this.cpuTime = cpuTime;
    }

    public long getAllocBytes() {
        return allocBytes;
    }

    public void setAllocBytes(long allocBytes) {
        this.allocBytes = allocBytes;
    }

    public static MyThreadInfo createProto(String procConnect, long threadId) {
        return new MyThreadInfo(procConnect, threadId);
}

    public String getProcConnect() {
        return procConnect;
    }

    public void setProcConnect(String procConnect) {
        this.procConnect = procConnect;
    }

    public String[] getStackTrace() {
        return StackTraceParser.parseStackTrace(stackTraceElems);
    }
}
