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


import javax.management.openmbean.CompositeData;

public class MyThreadInfo {
    protected long id;
    protected String name;
    protected long blockedTime;
    protected long blockedCount;
    protected long cpuTime;
    protected long allocBytes;

    public MyThreadInfo(CompositeData thread) {
        blockedTime = (Long) thread.get("blockedTime");
        blockedCount = (Long) thread.get("blockedCount");
        name = (String) thread.get("threadName");
        id = (Long) thread.get("threadId");
    }

    public MyThreadInfo(MyThreadInfo t1, MyThreadInfo t2) {
        blockedTime = t2.blockedTime - t1.blockedTime;
        blockedCount = t2.blockedCount - t1.blockedCount;
        cpuTime = t2.cpuTime - t1.cpuTime;
        allocBytes = t2.allocBytes - t1.allocBytes;
        name = t1.name;
        id = t1.id;
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
    
}
