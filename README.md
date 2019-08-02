[![CircleCI](https://circleci.com/gh/galeb/galeb/tree/master.svg?style=svg)](https://circleci.com/gh/galeb/galeb/tree/master)

GALEB 4

1. Using Makefile

1.1. Building docs (requires Doxygen)

# make doc

1.2. Building RH7/EL7 rpm (requires FPM)

# make dist

2. Installing

# mvn clean install -DskipTests

3. Running

3.1. Running router

# export ROUTER_PORT=8080
# export MANAGER_URL=http://manager.localhost:8000
# export GROUP_ID=blue
# export ENVIRONMENT_NAME=desenv
# cd router && mvn spring-boot:run

3.2. Running health

# export BROKER_CONN="tcp://broker.localhost:61616?blockOnDurableSend=false&consumerWindowSize=0&protocols=Core"
# cd health && mvn spring-boot:run

