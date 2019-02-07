pipeline {
  agent any
  stages {
    stage('get last packages') {
      steps {
        sh '''#!/bin/bash
version="$(curl -s -L -H \'Authorization: token \'${GITHUB_TOKEN} https://api.github.com/repos/galeb/galeb/releases/latest | tee /tmp/releases.json | jq -r .tag_name | sed \'s/^v//\')"
cat /tmp/releases.json
if [ "x${version}" != "x" -a "x${version}" != "xnull" ]; then
rm -f /tmp/*.rpm
for service in api legba kratos router health; do
package=galeb-${service}-${version}.el7.noarch.rpm
curl -s -k -I -w "%{http_code}" ${ARTIFACTORY_REPO}/${package} -o /dev/null | grep \'^200$\' > /dev/null
if [ $? -ne 0 ]; then
echo "ARTIFACTORY: Package ${package} not found. Getting from github"
curl -s -v -k -L -H \'Authorization: token \'${GITHUB_TOKEN} https://github.com/galeb/galeb/releases/download/v${version}/${package} -o /tmp/${package} || true
else
echo "ARTIFACTORY: Package ${package} found. Getting from local repo"
curl -s -v -k -L ${ARTIFACTORY_REPO}/${package} -o /tmp/${package} || true
fi
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
myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API}"
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-api-*.el7.noarch.rpm root@${GALEB_API}:/tmp
$myssh "/bin/yum clean all; /bin/yum install jdk1.8.0_144 -y; /bin/yum remove -y galeb-api && /bin/yum install -y /tmp/galeb-api-*.el7.noarch.rpm && rm -f /tmp/galeb-api-*.el7.noarch.rpm"
$myssh "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
$myssh "mkdir -p /opt/logs/galeb && chmod 777 -R /opt/logs/galeb || true"
$myssh "/usr/bin/systemctl enable galeb || true"
$myssh "/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5"
$myssh "/sbin/sysctl -w net.core.somaxconn=40000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000"
$myssh "/sbin/sysctl -w net.core.netdev_max_backlog=10000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_syncookies=1"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512"
$myssh "/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1"
$myssh "/sbin/sysctl -w net.ipv4.ip_local_port_range=\'15000 65000\'"
$myssh "/sbin/sysctl -w fs.file-max=100000"
$myssh "/sbin/swapoff -a; /bin/sed -i -e \'/.*swap.*/d\' /etc/fstab"'''
          }
        }
        stage('update LEGBA') {
          steps {
            sh '''#!/bin/bash
myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA}"
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-legba-*.el7.noarch.rpm root@${GALEB_LEGBA}:/tmp
$myssh "/bin/yum clean all; /bin/yum install jdk1.8.0_144 -y; /bin/yum remove -y galeb-legba && /bin/yum install -y /tmp/galeb-legba-*.el7.noarch.rpm && rm -f /tmp/galeb-legba-*.el7.noarch.rpm"
$myssh "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
$myssh "mkdir -p /opt/logs/galeb && chmod 777 -R /opt/logs/galeb || true"
$myssh "/usr/bin/systemctl enable galeb || true"
$myssh "/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5"
$myssh "/sbin/sysctl -w net.core.somaxconn=40000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000"
$myssh "/sbin/sysctl -w net.core.netdev_max_backlog=10000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_syncookies=1"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512"
$myssh "/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1"
$myssh "/sbin/sysctl -w net.ipv4.ip_local_port_range=\'15000 65000\'"
$myssh "/sbin/sysctl -w fs.file-max=100000"
$myssh "/sbin/swapoff -a; /bin/sed -i -e \'/.*swap.*/d\' /etc/fstab"'''
          }
        }
        stage('update KRATOS') {
          steps {
            sh '''#!/bin/bash
myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS}"
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-kratos-*.el7.noarch.rpm root@${GALEB_KRATOS}:/tmp
$myssh "/bin/yum clean all; /bin/yum install jdk1.8.0_144 -y; /bin/yum remove -y galeb-kratos && /bin/yum install -y /tmp/galeb-kratos-*.el7.noarch.rpm && rm -f /tmp/galeb-kratos-*.el7.noarch.rpm"
$myssh "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
$myssh "mkdir -p /opt/logs/galeb && chmod 777 -R /opt/logs/galeb || true"
$myssh "/usr/bin/systemctl enable galeb || true"
$myssh "/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5"
$myssh "/sbin/sysctl -w net.core.somaxconn=40000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000"
$myssh "/sbin/sysctl -w net.core.netdev_max_backlog=10000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_syncookies=1"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512"
$myssh "/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1"
$myssh "/sbin/sysctl -w net.ipv4.ip_local_port_range=\'15000 65000\'"
$myssh "/sbin/sysctl -w fs.file-max=100000"
$myssh "/sbin/swapoff -a; /bin/sed -i -e \'/.*swap.*/d\' /etc/fstab"'''
          }
        }
        stage('update ROUTER') {
          steps {
            sh '''#!/bin/bash
myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER}"
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-router-*.el7.noarch.rpm root@${GALEB_ROUTER}:/tmp
$myssh "/bin/yum clean all; /bin/yum install jdk1.8.0_144 -y; /bin/yum remove -y galeb-router && /bin/yum install -y /tmp/galeb-router-*.el7.noarch.rpm && rm -f /tmp/galeb-router-*.el7.noarch.rpm"
$myssh "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
$myssh "mkdir -p /opt/logs/galeb && chmod 777 -R /opt/logs/galeb || true"
$myssh "/usr/bin/systemctl enable galeb || true"
$myssh "/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5"
$myssh "/sbin/sysctl -w net.core.somaxconn=40000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000"
$myssh "/sbin/sysctl -w net.core.netdev_max_backlog=10000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_syncookies=1"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512"
$myssh "/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1"
$myssh "/sbin/sysctl -w net.ipv4.ip_local_port_range=\'15000 65000\'"
$myssh "/sbin/sysctl -w fs.file-max=100000"
$myssh "/sbin/swapoff -a; /bin/sed -i -e \'/.*swap.*/d\' /etc/fstab"'''
          }
        }
        stage('update HEALTH') {
          steps {
            sh '''#!/bin/bash
myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH}"
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-health-*.el7.noarch.rpm root@${GALEB_HEALTH}:/tmp
$myssh "/bin/yum clean all; /bin/yum install jdk1.8.0_144 -y; /bin/yum remove -y galeb-health && /bin/yum install -y /tmp/galeb-health-*.el7.noarch.rpm && rm -f /tmp/galeb-health-*.el7.noarch.rpm"
$myssh "id galeb > /dev/null 2>&1 || (groupadd galeb && useradd -g galeb -d /opt/galeb galeb)"
$myssh "mkdir -p /opt/logs/galeb && chmod 777 -R /opt/logs/galeb || true"
$myssh "/usr/bin/systemctl enable galeb || true"
$myssh "/sbin/sysctl -w net.ipv4.tcp_fin_timeout=5"
$myssh "/sbin/sysctl -w net.core.somaxconn=40000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_syn_backlog=20000"
$myssh "/sbin/sysctl -w net.core.netdev_max_backlog=10000"
$myssh "/sbin/sysctl -w net.ipv4.tcp_syncookies=1"
$myssh "/sbin/sysctl -w net.ipv4.tcp_max_tw_buckets=512"
$myssh "/sbin/sysctl -w net.ipv4.tcp_tw_reuse=1"
$myssh "/sbin/sysctl -w net.ipv4.ip_local_port_range=\'15000 65000\'"
$myssh "/sbin/sysctl -w fs.file-max=100000"
$myssh "/sbin/swapoff -a; /bin/sed -i -e \'/.*swap.*/d\' /etc/fstab"'''
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
    stage('Update DB Schema') {
      steps {
        sh '''#!/bin/bash

/usr/bin/mvn -Dhttps.proxyHost=$(echo $https_proxy|cut -d\'/\' -f3|cut -d\':\' -f1) -Dhttps.proxyPort=$(echo $https_proxy|cut -d\':\' -f3|cut -d\'/\' -f1) -Dhttp.proxyHost=$(echo $http_proxy|cut -d\'/\' -f3|cut -d\':\' -f1) -Dhttp.proxyPort=$(echo $http_proxy|cut -d\':\' -f3|cut -d\'/\' -f1) clean
cd newcore && \\
/usr/bin/mvn -Dhttps.proxyHost=$(echo $https_proxy|cut -d\'/\' -f3|cut -d\':\' -f1) -Dhttps.proxyPort=$(echo $https_proxy|cut -d\':\' -f3|cut -d\'/\' -f1) -Dhttp.proxyHost=$(echo $http_proxy|cut -d\'/\' -f3|cut -d\':\' -f1) -Dhttp.proxyPort=$(echo $http_proxy|cut -d\':\' -f3|cut -d\'/\' -f1) install
cd ../api && \\
/usr/bin/mvn -Dhttps.proxyHost=$(echo $https_proxy|cut -d\'/\' -f3|cut -d\':\' -f1) -Dhttps.proxyPort=$(echo $https_proxy|cut -d\':\' -f3|cut -d\'/\' -f1) -Dhttp.proxyHost=$(echo $http_proxy|cut -d\'/\' -f3|cut -d\':\' -f1) -Dhttp.proxyPort=$(echo $http_proxy|cut -d\':\' -f3|cut -d\'/\' -f1) \\
flyway:migrate -Dflyway.user=$GALEB_DB_USER -Dflyway.password=$GALEB_DB_PASS -Dflyway.url=$GALEB_DB_URL'''
      }
    }
    stage('Restart services') {
      parallel {
        stage('Start API') {
          steps {
            sh '''#!/bin/bash

myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API}"
$myssh "/etc/init.d/galeb restart"'''
          }
        }
        stage('Start LEGBA') {
          steps {
            sh '''#!/bin/bash

myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA}"
$myssh "/etc/init.d/galeb restart"'''
          }
        }
        stage('Start KRATOS') {
          steps {
            sh '''#!/bin/bash

myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS}"
$myssh "/etc/init.d/galeb restart"'''
          }
        }
        stage('Start ROUTER') {
          steps {
            sh '''#!/bin/bash

myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER}"
$myssh "/etc/init.d/galeb restart"'''
          }
        }
        stage('Start HEALTH') {
          steps {
            sh '''#!/bin/bash

myssh="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH}"
$myssh "/etc/init.d/galeb restart"'''
          }
        }
      }
    }
    stage('Artifactory Upload') {
      steps {
        sh '''#!/bin/bash

for package in /tmp/galeb-*rpm; do
echo $package
curl -s -k -I -w "%{http_code}" ${ARTIFACTORY_REPO}/${package##*/} -o /dev/null | grep \'^200$\' > /dev/null
if [ $? -ne 0 ]; then
curl -H \'X-JFrog-Art-Api:\'${ARTIFACTORY_TOKEN} -XPUT ${ARTIFACTORY_REPO}/${package##*/} -T ${package}
else	
echo "Package already exists: ${package##*/}. Ignoring upload."
fi
done'''
      }
    }
    stage('Performance Tests') {
      parallel {
        stage('Test API') {
          steps {
            sh '''#!/bin/bash

TOKEN="$(curl --noproxy \'*\' --silent -I -XGET -u ${GROU_USER}:${GROU_PASSWORD} ${ENDPOINT_GROU}/token/${GROU_PROJECT} | grep \'^x-auth-token:\' | awk \'{ print $2 }\')"
TOKEN_API="$(curl --noproxy \'*\' -XGET -u admin:admin ${GALEB_API}:8000/token | jq -r .token)"

# POST METHOD
# for file in $(ls $WORKSPACE/jenkins/api/*post.json); do
#   JSON=$(cat $file | tr -d \'\\n\' | sed "s,RANDOM,$RANDOM,g" | sed "s,GROU_PROJECT,$GROU_PROJECT," | sed "s,GROU_NOTIFY,$GROU_NOTIFY," | sed "s,GALEB_API,$GALEB_API,g" | sed "s,TOKEN_API,$TOKEN_API,")
#   #echo "$JSON"

#   RESULT_GROU=$(curl --noproxy \'*\' -H\'content-type:application/json\' -H"x-auth-token:$TOKEN" -XPOST -d"$JSON" ${ENDPOINT_GROU}/tests)

#   #echo $RESULT_GROU
#   TEST_STATUS=$(echo $RESULT_GROU | jq -r .status)
#   TEST_URL=$(echo $RESULT_GROU | jq -r ._links.self.href)
  
#   while [ "${TEST_STATUS}" != "OK" ]
#   do
#     TEST_STATUS=$(curl --noproxy \'*\' -H\'content-type:application/json\' $TEST_URL | jq -r .status)
#     echo $TEST_STATUS
#     sleep 5
#   done
  
# done

#BP and Environment created default ID: 1
# GALEB_BP_ID=$(curl --noproxy \'*\' -v -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"bp-$RANDOM\\"}" -u admin:admin ${GALEB_API}:8000/balancepolicy | jq -r .id)
# GALEB_ENVIRONMENT_ID=$(curl --noproxy \'*\' -v -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"environment-$RANDOM\\"}" -u admin:admin ${GALEB_API}:8000/environment | jq -r .id)


GALEB_TEAM_ID=$(curl --noproxy \'*\' -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"team-$RANDOM\\"}" -u admin:admin ${GALEB_API}:8000/team | jq -r .id)
echo $GALEB_TEAM_ID

#PROJECT OK
GALEB_PROJECT_ID=$(curl --noproxy \'*\' -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"project-$RANDOM\\",\\"teams\\" : [\\"http://${GALEB_API}/team/${GALEB_TEAM_ID}\\"]}" -u admin:admin ${GALEB_API}:8000/project | jq -r .id)
echo $GALEB_PROJECT_ID

#POOL OK
GALEB_POOL_ID=$(curl --noproxy \'*\' -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"pool-$RANDOM\\",\\"project\\" : \\"http://${GALEB_API}/project/${GALEB_PROJECT_ID}\\",\\"environment\\" : \\"http://${GALEB_API}/environment/1\\",\\"balancepolicy\\" : \\"http://${GALEB_API}/balancepolicy/1\\",\\"hc_tcp_only\\" : \\"true\\"}" -u admin:admin ${GALEB_API}:8000/pool | jq -r .id)
echo $GALEB_POOL_ID

#TARGET OK
GALEB_TARGET_ID=$(curl --noproxy \'*\' -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"target-$RANDOM\\",\\"pool\\" : \\"http://${GALEB_API}/pool/${GALEB_POOL_ID}\\"}" -u admin:admin ${GALEB_API}:8000/target | jq -r .id)
echo $GALEB_TARGET_ID

#VIRTUALHOST OK
GALEB_VIRTUALHOST_ID=$(curl --noproxy \'*\' -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"virtualhost-$RANDOM\\",\\"project\\" : \\"http://${GALEB_API}/project/${GALEB_PROJECT_ID}\\",\\"environments\\" : [\\"http://${GALEB_API}/environment/1\\"]}" -u admin:admin ${GALEB_API}:8000/virtualhost | jq -r .id)
echo $GALEB_VIRTUALHOST_ID

#RULE OK
GALEB_RULE_ID=$(curl --noproxy \'*\' -v -H\'content-type:application/json\' -X POST -d "{\\"name\\" : \\"rule-$RANDOM\\",\\"project\\" : \\"http://${GALEB_API}/project/${GALEB_PROJECT_ID}\\",\\"pools\\" : [\\"http://${GALEB_API}/pool/${GALEB_POOL_ID}\\"],\\"matching\\" : \\"/\\" }" -u admin:admin ${GALEB_API}:8000/rule | jq -r .id)
echo $GALEB_RULE_ID


# GET METHOD
# for file in $(ls $WORKSPACE/jenkins/api/*get.json); do
for file in $(ls /var/jenkins_home/workspace/galeb_jenkins/jenkins/api/*get.json); do

JSON=$(cat $file | tr -d \'\\n\' | sed "s,RANDOM,$RANDOM,g" | sed "s,GROU_PROJECT,$GROU_PROJECT," | sed "s,GROU_NOTIFY,$GROU_NOTIFY," | sed "s,GALEB_API,$GALEB_API,g" | sed "s,TOKEN_API,YWRtaW46YWRtaW4=,g" | sed "s,GALEB_TEAM_ID,$GALEB_TEAM_ID," | sed "s,GALEB_PROJECT_ID,$GALEB_PROJECT_ID,"| sed "s,GALEB_POOL_ID,$GALEB_POOL_ID," | sed "s,GALEB_VIRTUALHOST_ID,$GALEB_VIRTUALHOST_ID,"| sed "s,GALEB_RULE_ID,$GALEB_RULE_ID," | sed "s,GALEB_TARGET_ID,$GALEB_TARGET_ID,")
echo "$JSON"

RESULT_GROU=$(curl --noproxy \'*\' -H\'content-type:application/json\' -H"x-auth-token:$TOKEN" -XPOST -d"$JSON" ${ENDPOINT_GROU}/tests)
echo $RESULT_GROU
sleep 60

# curl --noproxy \'*\' -H\'content-type:application/json\' -X GET -u admin:admin ${GALEB_API}:8000/team/${GALEB_TEAM_ID} | jq -r .

#curl --noproxy \'*\' -H\'content-type:application/json\' -X DELETE - -u admin:admin ${GALEB_API}:8000/team/${GALEB_TEAM_ID} | jq -r .

done
'''
          }
        }
        stage('Test LEGBA') {
          steps {
            sh '''#!/bin/bash

#TOKEN="$(curl --silent -I -XGET -u ${GROU_USER}:${GROU_PASSWORD} ${ENDPOINT_GROU}/token/${GROU_PROJECT} | grep \'^x-auth-token:\' | awk \'{ print $2 }\')"

#JSON=$(cat $WORKSPACE/jenkins/galeb_legba.json | sed "s,RANDOM,$RANDOM," | sed "s,GROU_PROJECT,$GROU_PROJECT," | sed "s,GROU_NOTIFY,$GROU_NOTIFY," | sed "s,GALEB_LEGBA,$GALEB_LEGBA,g")

#echo "$JSON"

#curl -v -H\'content-type:application/json\' -H"x-auth-token:$TOKEN" -d"$JSON" ${ENDPOINT_GROU}/tests

#RESULT_GROU=$(curl -v -H\'content-type:application/json\' -H"x-auth-token:$TOKEN" -d"$JSON" ${ENDPOINT_GROU}/tests)

#RESULT_STATUS=$($RESULT_GROU | jq .)

#echo $RESULT_STATUS'''
          }
        }
        stage('Test KRATOS') {
          steps {
            sh '#'
          }
        }
        stage('Test ROUTER') {
          steps {
            sh '#'
          }
        }
        stage('Test HEALTH') {
          steps {
            sh '#'
          }
        }
      }
    }
  }
}