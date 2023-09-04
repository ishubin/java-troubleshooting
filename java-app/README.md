Java Troubleshooting
=====================



CPU profiling with Async Profiler
---------------------------------

If running in docker container you would have to use `--security-opt seccomp=unconfined` arguments.

```bash
PID=$(ps -ef | grep java | grep -v grep | awk '{print $2}')
./profiler.sh start -e cpu -i 7ms $PID
```


```bash
./profiler.sh dump -o collapsed $PID > log.collapsed
```




Various troubleshooting tools
-----------------------------------------


#### jemalloc

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