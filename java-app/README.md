Java Troubleshooting
=====================



CPU profiling with Async Profiler
---------------------------------

If trying this in docker container you would have to run your container with `--security-opt seccomp=unconfined` arguments.

```bash
PID=$(ps -ef | grep java | grep -v grep | awk '{print $2}')
./profiler.sh start -e cpu -i 7ms $PID
```


```bash
./profiler.sh dump -o collapsed $PID > log.collapsed
```

Once the collapsed log is obtained you can generate a [flame graph](https://www.brendangregg.com/flamegraphs.html) 
for further analysis using one of the following tools:

* [flame-graph-reader](https://ishubin.github.io/flame-graph-reader/) - In-browser flame graph visualizer with various features like: annotations, search, flame graph comparison etc.
* [speedscope.app](https://www.speedscope.app/) - high performance fast in-browser flame graph visualizer
* [Brendan Gregg's original flame graph svg generator](https://github.com/brendangregg/FlameGraph) - Perl script that generates flame graph SVG


Memory allocation with Async Profiler
-------------------------------------

TBD

Method profiling with Async Profiler
-------------------------------------

TBD


Troubleshooting tools
---------------------

### async-profiler

[async-profiler](https://github.com/async-profiler/async-profiler) is a low overhead sampling profiler for Java that does not suffer from Safepoint bias problem. 
It features HotSpot-specific APIs to collect stack traces and to track memory allocations.
The profiler works with OpenJDK, Oracle JDK and other Java runtimes based on the HotSpot JVM.

async-profiler can trace the following kinds of events:

* CPU cycles
* Hardware and Software performance counters like cache misses, branch misses, page faults, context switches etc.
* Allocations in Java Heap
* Contented lock attempts, including both Java object monitors and ReentrantLocks


##### Installing async-profiler

Download tar.gz archive from https://github.com/async-profiler/async-profiler/releases and extract it. That's it.


##### Using async-profiler

You can find all the profiling options with examples documented in [README file](https://github.com/async-profiler/async-profiler#async-profiler)



### jemalloc

[Jemalloc](https://jemalloc.net/) -  is a general purpose malloc(3) implementation that emphasizes fragmentation avoidance and scalable concurrency support.

##### Installing jemalloc

Download archive from https://github.com/jemalloc/jemalloc/releases and extract it.
Go into extracted folder and run the follow these instructions https://github.com/jemalloc/jemalloc/blob/dev/INSTALL.md

```bash
./configure --enable-prof
make
make install
```

That's it

##### Using jemalloc

The simplest way to use jemalloc is described here https://github.com/jemalloc/jemalloc/wiki/Getting-Started

```bash
# configuring your app to use jemalloc instead of malloc
export LD_PRELOAD=`jemalloc-config --libdir`/libjemalloc.so.`jemalloc-config --revision`

# configuring jemalloc to profile malloc invocations
export MALLOC_CONF=prof:true,lg_prof_interval:30,lg_prof_sample:17
```

```bash
jeprof --show_bytes --gif "$(which java)" jeprof*.heap > /tmp/malloc-profiling.gif
```

```bash
jeprof --show_bytes --collapsed "$(which java)" jeprof*.heap > /tmp/malloc-bytes-collapsed.log
```