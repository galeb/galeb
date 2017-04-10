GALEB 4

1. Using etcd docker service

# docker run --rm --name myetcd -d -p 2379:2379 -p 2380:2380 -p 4001:4001 -p 7001:7001 elcolio/etcd:latest -name etcd0

2. Using statsd docker service

# docker run -d --rm --name mystatsd -p 8125:8125 -p 8125:8125/udp -v /tmp:/tmp -e STREAM_CMD="tee /tmp/out" sstarcher/statsite

3. Accessing etcd docker service

# docker exec -it myetcd /bin/sh

4. Defining routes in etcd service

# etcdctl mkdir /GALEB-BE1/virtualhosts/test.com/rules/$(echo '/' | base64)
# etcdctl set /GALEB-BE1/virtualhosts/test.com/rules/$(echo '/' | base64)/order 0
# etcdctl set /GALEB-BE1/virtualhosts/test.com/rules/$(echo '/' | base64)/target 0
# etcdctl set /GALEB-BE1/virtualhosts/test.com/rules/$(echo '/' | base64)/type PATH
# etcdctl set /GALEB-BE1/virtualhosts/test.com/allow 127.0.0.0/8,172.16.0.1
# etcdctl mkdir /GALEB-BE1/pools/0/targets
# etcdctl set /GALEB-BE1/pools/0/loadbalance ROUNDROBIN
# etcdctl set /GALEB-BE1/pools/0/targets/0 http://127.0.0.1:8080

5. Resetting all

# etcdctl set /GALEB-BE1/reset_all 0 --ttl 5

6. Resetting only one virtualhost

# etcdctl set /GALEB-BE1/virtualhosts/test.com/reset test.com --ttl 5
