FROM maven:3.9-eclipse-temurin-11-focal as builder

RUN mkdir /app

COPY . /app/

RUN cd /app && mvn clean package


FROM azul/zulu-openjdk:11-latest

ARG JEMALLOC_VERSION=5.3.0
ARG OTEL_AGENT_VERSION=1.29.0

RUN apt-get update && apt-get install -y wget bzip2 make build-essential graphviz tcpdump net-tools iftop curl sysstat  && rm -rf /var/lib/apt/lists/*

RUN mkdir /app && mkdir /async-profiler

RUN cd /async-profiler && \
    wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/async-profiler-2.9-linux-x64.tar.gz && \
    tar xvzf async-profiler-2.9-linux-x64.tar.gz


RUN mkdir /jemalloc && \
    cd /jemalloc && \
    wget https://github.com/jemalloc/jemalloc/releases/download/${JEMALLOC_VERSION}/jemalloc-${JEMALLOC_VERSION}.tar.bz2 && \
    bzip2 -d jemalloc-${JEMALLOC_VERSION}.tar.bz2 && \
    tar -xvf jemalloc-${JEMALLOC_VERSION}.tar && \
    cd jemalloc-${JEMALLOC_VERSION} && \
    ./configure && \
    make && \
    make install

RUN wget -O /opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_AGENT_VERSION}/opentelemetry-javaagent.jar

COPY --from=builder /app/target/java-app-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

COPY entry.sh /app/entry.sh

RUN chmod +x /app/entry.sh

ENV JAVA_XMX="64m"
ENV JAVA_XMS="64m"
ENV JAVA_GC="-XX:+UseG1GC"
ENV OTEL_EXPORTER_ENDPOINT="http://localhost:4318"

WORKDIR /app
ENTRYPOINT [ "bash", "/app/entry.sh" ]

