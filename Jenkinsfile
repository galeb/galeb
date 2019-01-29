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
    stage('update rpm') {
      parallel {
        stage('update API') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-api-*.el7.noarch.rpm root@${GALEB_API}:/tmp
rm -f /tmp/galeb-api-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API} "/bin/yum clean all; /bin/yum remove -y galeb-api && /bin/yum install -y /tmp/galeb-api-*.el7.noarch.rpm && rm -f /tmp/galeb-api-*.el7.noarch.rpm"
 '''
          }
        }
        stage('update LEGBA') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-legba-*.el7.noarch.rpm root@${GALEB_LEGBA}:/tmp
rm -f /tmp/galeb-legba-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA} "/bin/yum clean all; /bin/yum remove -y galeb-legba && /bin/yum install -y /tmp/galeb-legba-*.el7.noarch.rpm && rm -f /tmp/galeb-legba-*.el7.noarch.rpm"
 '''
          }
        }
        stage('update KRATOS') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-kratos-*.el7.noarch.rpm root@${GALEB_KRATOS}:/tmp
rm -f /tmp/galeb-kratos-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS} "/bin/yum clean all; /bin/yum remove -y galeb-kratos && /bin/yum install -y /tmp/galeb-kratos-*.el7.noarch.rpm && rm -f /tmp/galeb-kratos-*.el7.noarch.rpm"
 '''
          }
        }
        stage('update ROUTER') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-router-*.el7.noarch.rpm root@${GALEB_ROUTER}:/tmp
rm -f /tmp/galeb-router-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER} "/bin/yum clean all; /bin/yum remove -y galeb-router && /bin/yum install -y /tmp/galeb-router-*.el7.noarch.rpm && rm -f /tmp/galeb-router-*.el7.noarch.rpm"
 '''
          }
        }
        stage('update HEALTH') {
          steps {
            sh '''#!/bin/bash
scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-health-*.el7.noarch.rpm root@${GALEB_HEALTH}:/tmp
rm -f /tmp/galeb-health-*.el7.noarch.rpm || true
ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH} "/bin/yum clean all; /bin/yum remove -y galeb-health && /bin/yum install -y /tmp/galeb-health-*.el7.noarch.rpm && rm -f /tmp/galeb-health-*.el7.noarch.rpm"
 '''
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