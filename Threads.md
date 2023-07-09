Threads analysis
====================

```
jcmd 1 Thread.print
```


Breakdown cpu and memory usage by thread
-----------------------------------------

```
top - 18:57:26 up  1:12,  0 users,  load average: 0.05, 0.09, 0.03
Threads:  32 total,   0 running,  32 sleeping,   0 stopped,   0 zombie
%Cpu(s):  0.1 us,  0.1 sy,  0.0 ni, 99.8 id,  0.0 wa,  0.0 hi,  0.0 si,  0.0 st
MiB Mem :   7851.5 total,   5599.7 free,    407.7 used,   1844.1 buff/cache
MiB Swap:   1024.0 total,   1024.0 free,      0.0 used.   6923.9 avail Mem

  PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND
    8 root      20   0 5699120  57816  16656 S   0.3   0.7   0:00.02 GC Thread#0
   21 root      20   0 5699120  57816  16656 S   0.3   0.7   0:00.11 VM Periodic Tas
    1 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 java
    7 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.09 java
    9 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 G1 Main Marker
   10 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 G1 Conc#0
   11 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 G1 Refine#0
   12 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.02 G1 Young RemSet
   13 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.02 VM Thread
   14 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Reference Handl
   15 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Finalizer
   16 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Signal Dispatch
   17 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Service Thread
   18 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.06 C2 CompilerThre
   19 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.11 C1 CompilerThre
   20 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Sweeper thread
   22 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Common-Cleaner
   23 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.10 Thread-0
   24 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 qtp1358843893-1
   25 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 qtp1358843893-1
   26 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 qtp1358843893-1
   27 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 qtp1358843893-1
   28 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 qtp1358843893-1
   29 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 qtp1358843893-1
   30 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.02 qtp1358843893-1
   31 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.05 qtp1358843893-2
   32 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Session-HouseKe
   33 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 Connector-Sched
   34 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 GC Thread#1
   35 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.00 GC Thread#2
   36 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.01 GC Thread#3
   37 root      20   0 5699120  57816  16656 S   0.0   0.7   0:00.01 GC Thread#4
```

Now take a PID (e.g. `32` for `Session-HouseKe` thread) and convert it heximal representation

```
printf "%x\n" 32
20
```

Now you can take the heximal representation of thread id and search for it in thread dump:

```
jcmd 1 Thread.print | grep 'nid=0x20' -A 12

"Session-HouseKeeper-5a2c9810-1" #21 prio=5 os_prio=0 cpu=0.25ms elapsed=233.40s tid=0x0000ffff5413d000 nid=0x20 waiting on condition  [0x0000ffff4effd000]
   java.lang.Thread.State: TIMED_WAITING (parking)
	at jdk.internal.misc.Unsafe.park(java.base@11.0.19/Native Method)
	- parking to wait for  <0x000000008c10a718> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	at java.util.concurrent.locks.LockSupport.parkNanos(java.base@11.0.19/LockSupport.java:234)
	at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.awaitNanos(java.base@11.0.19/AbstractQueuedSynchronizer.java:2123)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(java.base@11.0.19/ScheduledThreadPoolExecutor.java:1182)
	at java.util.concurrent.ScheduledThreadPoolExecutor$DelayedWorkQueue.take(java.base@11.0.19/ScheduledThreadPoolExecutor.java:899)
	at java.util.concurrent.ThreadPoolExecutor.getTask(java.base@11.0.19/ThreadPoolExecutor.java:1054)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(java.base@11.0.19/ThreadPoolExecutor.java:1114)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(java.base@11.0.19/ThreadPoolExecutor.java:628)
	at java.lang.Thread.run(java.base@11.0.19/Thread.java:829)
```


