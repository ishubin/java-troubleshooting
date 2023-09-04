#!/bin/bash

_term() {
  echo "Caught SIGTERM signal!"
  kill -TERM "$jpid" 2>/dev/null
}

trap _term SIGTERM

if [[ "JEMALLOC" -eq "1" ]]; then
  export LD_PRELOAD=`jemalloc-config --libdir`/libjemalloc.so.`jemalloc-config --revision`
  export MALLOC_CONF=prof:true,lg_prof_interval:30,lg_prof_sample:17
fi

java -Xmx64m -XX:NativeMemoryTracking=detail \
     -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+PreserveFramePointer \
     -Xlog:gc*=debug:file=/app/gc.log:time,uptimemillis,pid,tid,level,tags:filecount=5,filesize=100m \
     -jar /app/app.jar &

jpid=$!
wait "$jpid"
