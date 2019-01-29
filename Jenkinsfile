pipeline {
  agent any
  stages {
    stage('get last packages') {
      steps {
        sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
if [ "x${version}" != "x" ]; then
rm -f /tmp/*.rpm
for service in api legba kratos router health; do
curl -s -v -k -L https://github.com/galeb/galeb/releases/download/v${version}/galeb-${service}-${version}.el7.noarch.rpm -o /tmp/galeb-${service}-${version}.el7.noarch.rpm || true
done
else
exit 1
fi'''
      }
    }
    stage('setup') {
      parallel {
        stage('update API') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-api-*.el7.noarch.rpm root@${GALEB_API}:/tmp
rm -f /tmp/galeb-api-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API} "/bin/yum clean all; /bin/yum remove -y galeb-api && /bin/yum install -y /tmp/galeb-api-*.el7.noarch.rpm && rm -f /tmp/galeb-api-*.el7.noarch.rpm"
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API} "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
mkdir -p /opt/logs/galeb || true
chmod 777 -R /opt/logs/galeb || true
/usr/bin/systemctl enable galeb || true
/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5
/sbin/sysctl -w net.core.somaxconn=40000
/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000
/sbin/sysctl -w net.core.netdev_max_backlog=10000
/sbin/sysctl -w net.ipv4.tcp_syncookies=1
/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512
/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1
/sbin/sysctl -w net.ipv4.ip_local_port_range="15000 65000"
/sbin/sysctl -w fs.file-max=100000
swapoff -a
sed -i -e \'/.*swap.*/d\' /etc/fstab'''
          }
        }
        stage('update LEGBA') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-legba-*.el7.noarch.rpm root@${GALEB_LEGBA}:/tmp
rm -f /tmp/galeb-legba-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA} "/bin/yum clean all; /bin/yum remove -y galeb-legba && /bin/yum install -y /tmp/galeb-legba-*.el7.noarch.rpm && rm -f /tmp/galeb-legba-*.el7.noarch.rpm"
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA} "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
mkdir -p /opt/logs/galeb || true
chmod 777 -R /opt/logs/galeb || true
/usr/bin/systemctl enable galeb || true
/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5
/sbin/sysctl -w net.core.somaxconn=40000
/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000
/sbin/sysctl -w net.core.netdev_max_backlog=10000
/sbin/sysctl -w net.ipv4.tcp_syncookies=1
/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512
/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1
/sbin/sysctl -w net.ipv4.ip_local_port_range="15000 65000"
/sbin/sysctl -w fs.file-max=100000
swapoff -a
sed -i -e \'/.*swap.*/d\' /etc/fstab'''
          }
        }
        stage('update KRATOS') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-kratos-*.el7.noarch.rpm root@${GALEB_KRATOS}:/tmp
rm -f /tmp/galeb-kratos-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS} "/bin/yum clean all; /bin/yum remove -y galeb-kratos && /bin/yum install -y /tmp/galeb-kratos-*.el7.noarch.rpm && rm -f /tmp/galeb-kratos-*.el7.noarch.rpm"
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS} "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
mkdir -p /opt/logs/galeb || true
chmod 777 -R /opt/logs/galeb || true
/usr/bin/systemctl enable galeb || true
/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5
/sbin/sysctl -w net.core.somaxconn=40000
/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000
/sbin/sysctl -w net.core.netdev_max_backlog=10000
/sbin/sysctl -w net.ipv4.tcp_syncookies=1
/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512
/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1
/sbin/sysctl -w net.ipv4.ip_local_port_range="15000 65000"
/sbin/sysctl -w fs.file-max=100000
swapoff -a
sed -i -e \'/.*swap.*/d\' /etc/fstab'''
          }
        }
        stage('update ROUTER') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-router-*.el7.noarch.rpm root@${GALEB_ROUTER}:/tmp
rm -f /tmp/galeb-router-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER} "/bin/yum clean all; /bin/yum remove -y galeb-router && /bin/yum install -y /tmp/galeb-router-*.el7.noarch.rpm && rm -f /tmp/galeb-router-*.el7.noarch.rpm"
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER} "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
mkdir -p /opt/logs/galeb || true
chmod 777 -R /opt/logs/galeb || true
/usr/bin/systemctl enable galeb || true
/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5
/sbin/sysctl -w net.core.somaxconn=40000
/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000
/sbin/sysctl -w net.core.netdev_max_backlog=10000
/sbin/sysctl -w net.ipv4.tcp_syncookies=1
/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512
/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1
/sbin/sysctl -w net.ipv4.ip_local_port_range="15000 65000"
/sbin/sysctl -w fs.file-max=100000
swapoff -a
sed -i -e \'/.*swap.*/d\' /etc/fstab'''
          }
        }
        stage('update HEALTH') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-health-*.el7.noarch.rpm root@${GALEB_HEALTH}:/tmp
rm -f /tmp/galeb-health-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH} "/bin/yum clean all; /bin/yum remove -y galeb-health && /bin/yum install -y /tmp/galeb-health-*.el7.noarch.rpm && rm -f /tmp/galeb-health-*.el7.noarch.rpm"
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH} "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
mkdir -p /opt/logs/galeb || true
chmod 777 -R /opt/logs/galeb || true
/usr/bin/systemctl enable galeb || true
/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5
/sbin/sysctl -w net.core.somaxconn=40000
/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000
/sbin/sysctl -w net.core.netdev_max_backlog=10000
/sbin/sysctl -w net.ipv4.tcp_syncookies=1
/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512
/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1
/sbin/sysctl -w net.ipv4.ip_local_port_range="15000 65000"
/sbin/sysctl -w fs.file-max=100000
swapoff -a
sed -i -e \'/.*swap.*/d\' /etc/fstab'''
          }
        }
      }
    }
    stage('update apikeys') {
      parallel {
        stage('update apikeys API') {
          steps {
            sh '''#!/bin/bash

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API} "cd /root && /root/makeenvs.sh api.galeb.qa02.globoi.com lab api"'''
          }
        }
        stage('update apikeys LEGBA') {
          steps {
            sh '''#!/bin/bash

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA} "cd /root && /root/makeenvs.sh legba.galeb.qa02.globoi.com lab legba"'''
          }
        }
        stage('update apikeys KRATOS') {
          steps {
            sh '''#!/bin/bash

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS} "cd /root && /root/makeenvs.sh kratos.galeb.qa02.globoi.com lab kratos"'''
          }
        }
        stage('update apikeys ROUTER') {
          steps {
            sh '''#!/bin/bash

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER} "cd /root && /root/makeenvs.sh be-qa2.router.cmal08.galeb.qa02.globoi.com lab router"'''
          }
        }
        stage('update apikeys HEALTH') {
          steps {
            sh '''#!/bin/bash

ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH} "cd /root && /root/makeenvs.sh be-qa2.health.cmal08.galeb.qa02.globoi.com lab health"'''
          }
        }
      }
    }
  }
}