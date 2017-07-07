GALEB 4

1. Using Makefile

1.1. Building docs

# make doc

1.2. Building rpm

# make dist

2. Installing

# mvn clean install -DskipTests

3. Running

3.1. Running router

# export ROUTER_PORT=8080
# export MANAGER_URL=http://manager.localhost:8000
# export GROUP_ID=blue
# export ENVIRONMENT_NAME=desenv
# cd router && mvn clean package -DskipTests spring-boot:run

3.2. Running health

# export BROKER_CONN="tcp://broker.localhost:61616?blockOnDurableSend=false&consumerWindowSize=0&protocols=Core"
# cd health && mvn clean package -DskipTests spring-boot:run

