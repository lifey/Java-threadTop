Java-ThreadTop
==============

Java-ThreadTop is a simple command line tool which uses JMX protocol to connect to a Java process.
After the connection is established it will try to find the threads which consume most of the process resources.
The threads are sorted either by CPU consumption, by contention time(locking) or by bytes allocated. The tool uses Hotspot JMX APIs to extract the relevant data. 
The tool is provided with source code under the LGPL license. 
The source code is located at github. Github project is https://github.com/lifey/Java-threadTop.git  

While very simple it can be a very powerful aid when used together with jstack. It can help focus on the threads which consume the highest CPU or suffer from most contention.
Java-ThreadTop can connect to the process by either remote-JMX or Attach mechanism (Java 1.6 and above)

For Java 1.5 and for remote monitoring the monitored process need to be configured with remote JMX enabled:
   -Dcom.sun.management.jmxremote
   -Dcom.sun.management.jmxremote.port=54321
   -Dcom.sun.management.jmxremote.authenticate=false
   -Dcom.sun.management.jmxremote.ssl=false
(The tool supports authentication)

If process is on local machine and process supports attach mechanism (Java 1.6 and above ) tool may connect via process id

Note: This version is a preliminary initial implementation comments are welcome....

Two scripts are are provided threadTop.bat for Windows and threadTop.sh for UNIX and MacOS systems. In order to run it correctly JAVA_HOME need to be set to the JDK directory (threadTop relies on tools.jar and needs it for attach purposes)

Usage:
=====
The last parameters of the command line will always be a list of process ids or machineName:Port
ThreadTop command line parameters :
 [--addAllThreadsAndWindowsPerfMonData -w]  --> Not implemented yet.
 --iterations -i value : Number of iterations [default:1]
 [--measureThreadAlloc -a]
 [--measureThreadCPU -c]
 [--measureThreadContention -d]
 --num -n value : Number of top threads to show[default:1]
 --password -p value : Set password for remote connect [optional]
 --regExp -r value : Thread name regex filter [default:.*]
 --sort -s value : Sort by CPU/CONTEND/ALLOC/NAME [default:CPU]
 --timeToMeasure -m value : Set amount of time to measure in milliseconds [default:5000]
 --user -u value : Set user for remote connect [optional]

