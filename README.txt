GALEB 4

1. Using statsd docker service

# docker run -d --rm --name mystatsd -p 8125:8125 -p 8125:8125/udp -v /tmp:/tmp -e STREAM_CMD="tee /tmp/out" sstarcher/statsite

