pipeline {
  agent any
  stages {
    stage('get last packages') {
      environment {
        http_proxy = 'http://proxy.globoi.com:3128'
        https_proxy = 'http://proxy.globoi.com:3128'
        no_proxy = 'localhost,127.0.0.1,globoi.com'
      }
      steps {
        sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
rm -f /tmp/*.rpm
for service in api legba kratos router health; do
curl -s -v -k -L https://github.com/galeb/galeb/releases/download/v${version}/galeb-${service}-${version}.el7.noarch.rpm -o /tmp/galeb-${service}-${version}.el7.noarch.rpm || true
done'''
      }
    }
    stage('update API') {
      parallel {
        stage('update API') {
          environment {
            GALEB_API = '10.224.158.131'
          }
          steps {
            sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
sshpass -p \'ChangeMe\' scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-api-${version}.el7.noarch.rpm root@${GALEB_API}:/tmp
sshpass -p \'ChangeMe\' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_API} "/bin/yum clean all; /bin/yum remove -y galeb-api && /bin/yum install -y /tmp/galeb-api-${version}.el7.noarch.rpm && rm -f /tmp/galeb-api-${version}.el7.noarch.rpm"
 '''
          }
        }
        stage('update LEGBA') {
          environment {
            GALEB_LEGBA = '10.224.158.137'
          }
          steps {
            sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
sshpass -p \'ChangeMe\' scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-legba-${version}.el7.noarch.rpm root@${GALEB_LEGBA}:/tmp
sshpass -p \'ChangeMe\' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_LEGBA} "/bin/yum clean all; /bin/yum remove -y galeb-legba && /bin/yum install -y /tmp/galeb-legba-${version}.el7.noarch.rpm && rm -f /tmp/galeb-legba-${version}.el7.noarch.rpm"
 '''
          }
        }
        stage('update KRATOS') {
          environment {
            GALEB_KRATOS = '10.224.158.143'
          }
          steps {
            sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
sshpass -p \'ChangeMe\' scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-kratos-${version}.el7.noarch.rpm root@${GALEB_KRATOS}:/tmp
sshpass -p \'ChangeMe\' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_KRATOS} "/bin/yum clean all; /bin/yum remove -y galeb-kratos && /bin/yum install -y /tmp/galeb-kratos-${version}.el7.noarch.rpm && rm -f /tmp/galeb-kratos-${version}.el7.noarch.rpm"
 '''
          }
        }
        stage('update ROUTER') {
          environment {
            GALEB_ROUTER = '10.224.158.156'
          }
          steps {
            sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
sshpass -p \'ChangeMe\' scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-router-${version}.el7.noarch.rpm root@${GALEB_ROUTER}:/tmp
sshpass -p \'ChangeMe\' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_ROUTER} "/bin/yum clean all; /bin/yum remove -y galeb-router && /bin/yum install -y /tmp/galeb-router-${version}.el7.noarch.rpm && rm -f /tmp/galeb-router-${version}.el7.noarch.rpm"
 '''
          }
        }
        stage('update HEALTH') {
          environment {
            GALEB_HEALTH = '10.224.158.149'
          }
          steps {
            sh '''#!/bin/bash
version="$(curl -s -L https://api.github.com/repos/galeb/galeb/releases/latest | jq -r .tag_name | sed \'s/^v//\')"
sshpass -p \'ChangeMe\' scp -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no /tmp/galeb-health-${version}.el7.noarch.rpm root@${GALEB_HEALTH}:/tmp
sshpass -p \'ChangeMe\' ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@${GALEB_HEALTH} "/bin/yum clean all; /bin/yum remove -y galeb-health && /bin/yum install -y /tmp/galeb-health-${version}.el7.noarch.rpm && rm -f /tmp/galeb-health-${version}.el7.noarch.rpm"
 '''
          }
        }
      }
    }
  }
}