# Tools for Testing

## Router

For testing the router, the easiest way is to run a dummy legba server. The dummy server opens two http servers, one on `:8080` with the routes needed by _galeb-router_, and another one for testing on `:8090` that serves `/*.ghtml` with a random sleep.

1. Build galeb from the root folder:
```
make galeb
```

2. On a terminal run the dummy server
```
go run tools/legba_dummy.go
```

3. On another terminal run _galeb-router_
```
./tools/env_var.sh
java -jar router/target/galeb-router-0.0.0-SNAPSHOT.jar
```

Options for running _galeb-router_:
- Logger: `-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -DAsyncLogger.WaitStrategy=Sleep`
- GC: `-XX:+UseParallelGC -XX:+PerfDisableSharedMem`
- Memory: `-Xms1024m -Xmx1024m`
