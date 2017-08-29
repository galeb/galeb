#!/bin/bash

/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5
/sbin/sysctl -w net.core.somaxconn=40000
/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000
/sbin/sysctl -w net.core.netdev_max_backlog=10000
/sbin/sysctl -w net.ipv4.tcp_syncookies=1
/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512
/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1
/sbin/sysctl -w net.ipv4.ip_local_port_range="15000 65000"
/sbin/sysctl -w fs.file-max=100000
