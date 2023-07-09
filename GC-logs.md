GC Logs
===========



##### Enable GC logs for Java 9+

Starting from Java version 9 the GC logs should be enabled using `-Xlog` option. The following `-Xlog` option provide good overview of GC in Java:

```
java -Xmx64m -Xlog:gc*=debug:file=/app/gc.log:time,uptimemillis,pid,tid,level,tags:filecount=5,filesize=100m -jar /app/app.jar
```

Once the logs are collected you can upload them on [GCEasy](https://gceasy.io/) (a useful online tool for Java GC logs analysis)



##### Calculating total pause time in some period of time

```
cat gc.log  | grep 'Pause' | grep M | sed -E 's/^(.*):[0-9]{2}\.[0-9]{3}\+[0-9]{4}\].*([0-9]+\.[0-9]+)ms$/\1 \2/g' | grep 2023-07-09T08:10 | awk 'BEGIN{total=0}{total+=$2}END{print total}'
```