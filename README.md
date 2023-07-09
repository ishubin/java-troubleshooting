Java Troubleshooting Tips
==========================


- [Tread analysis](/Threads.md)
- [CPU Profiling](/CPU-profiling.md)
- [Heap (memory) analysis](/CPU-profiling.md)


JVM Basics
----------------

- jstack
- jstat
- jcmd
- jmap



Garbage Collection and GC logs
-------------------------------

```
cat gc.log  | grep 'Pause' | grep M | sed -E 's/^(.*):[0-9]{2}\.[0-9]{3}\+[0-9]{4}\].*([0-9]+\.[0-9]+)ms$/\1 \2/g' | grep 2023-07-10T08:10 | awk 'BEGIN{total=0}{total+=$2}END{print total}'
```


Heap dump analysis
-------------------





CPU Profiling
-------------
