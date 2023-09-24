CPU Profilining
=================


Async Profiler: https://github.com/async-profiler/async-profiler


Async Profiler
-------------------


##### Starting CPU profiling

```
./profiler.sh start -e cpu -i 7ms 1
```


##### Dumping collapsed CPU profiling log

```
./profiler.sh dump -o collapsed 1 > log.collapsed
```


##### Allocation profiling for 30 seconds

```
./profiler.sh -e alloc -d 30 -f alloc_profile.html $JAVA_PID
```


##### Method profiling

```
./profiler.sh -e java.util.Properties.getProp 1
```

You can even profile invocations of `malloc` function:
```
./profiler.sh -e malloc 1
```