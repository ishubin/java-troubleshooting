FROM maven:3.9-eclipse-temurin-11-focal as builder

RUN mkdir /app

COPY . /app/

RUN cd /app && mvn clean package


FROM azul/zulu-openjdk:11-latest

RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

RUN mkdir /app && mkdir /async-profiler

RUN cd /async-profiler && \
    wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/async-profiler-2.9-linux-x64.tar.gz && \
    tar xvzf async-profiler-2.9-linux-x64.tar.gz

COPY --from=builder /app/target/java-app-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

RUN echo "#/bin/bash" > /app/start.sh && \
    echo "java -Xmx64m -Xlog:gc*=debug:file=/app/gc.log:time,uptimemillis,pid,tid,level,tags:filecount=5,filesize=100m -jar /app/app.jar " >> /app/start.sh && \
    chmod +x /app/start.sh

WORKDIR /app
ENTRYPOINT [ "bash", "/app/start.sh" ]

