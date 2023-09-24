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

if [[ -z "$OTEL_EXPORTER" ]]; then
    OTEL_EXPORTER="otlp"
fi

if [[ -z "$OTEL_EXPORTER_ENDPOINT" ]]; then
    OTEL_EXPORTER_ENDPOINT="http://localhost:4318"
fi

if [[ -z "$OTEL_EXPORTER_PROTOCOL" ]]; then
    OTEL_EXPORTER_PROTOCOL="http/protobuf"
fi

java -javaagent:/opentelemetry-javaagent.jar \
  -Dotel.service.name=japp \
  -Dotel.javaagent.debug=false \
  -Dotel.traces.exporter=${OTEL_EXPORTER} \
  -Dotel.metrics.exporter=${OTEL_EXPORTER} \
  -Dotel.exporter.otlp.protocol=${OTEL_EXPORTER_PROTOCOL} \
  -Dotel.exporter.otlp.traces.protocol=${OTEL_EXPORTER_PROTOCOL} \
  -Dotel.exporter.otlp.endpoint=${OTEL_EXPORTER_ENDPOINT} \
  -Dotel.propagators=b3multi \
  -Xmx64m -XX:NativeMemoryTracking=detail \
  -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints -XX:+PreserveFramePointer \
  -Xlog:gc*=debug:file=/app/gc.log:time,uptimemillis,pid,tid,level,tags:filecount=5,filesize=100m \
  -jar /app/app.jar &

jpid=$!
wait "$jpid"

