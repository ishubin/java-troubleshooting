Heap analysis
---------------


```
jcmd $JAVA_PID GC.heap_dump
```


Eclipse Memory Analyzer https://wiki.eclipse.org/MemoryAnalyzer/OQL

```
select * from "java.lang.String" s where toString(s) = "user.name"
```

```
select * from "java.lang.String" s where toString(s).contains("user.")
```

```
select * from "java.lang.String" s where toString(s).startsWith("user.")
```


```
select * from ".*" s where s.@usedHeapSize > 53687
```