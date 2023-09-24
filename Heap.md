Heap analysis
---------------


```
jcmd $JAVA_PID GC.heap_dump
```


Eclipse Memory Analyzer https://wiki.eclipse.org/MemoryAnalyzer/OQL

If you are having issues with opening Eclipse Memory Analyzer, it might be due to low memory settings. In that case you need to adjust its memory settings in `MemoryAnalyzer.ini` file. Find the `MemoryAnalyzer.ini` file, open it and increase `-Xmx` heap setting (e.g. `-Xmx4096m`). On MacOS you can find this configuration file at `/Applications/mat.app/Contents/Eclipse/MemoryAnalyzer.ini`


##### OQL tips

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