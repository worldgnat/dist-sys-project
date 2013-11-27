#!/bin/bash

export CLASSPATH=$(pwd)/D1ServerSrc:$(pwd)/jgroups-3.5.0.Alpha1.jar
killall -u $(whoami) rmiregistry && sleep 1
IP=$(ifconfig | grep eth0 -A 10 | grep "inet addr:" | sed -e 's/[^:]*:\([^ ]*\).*/\1/')
#LOGGING="-Djava.util.logging.config.file=logger.properties"
LOGGING="-Djgroups.udp.ip_ttl=128"
rmiregistry $1 &
if [ $# -eq 2 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Djava.rmi.server.codebase=$(pwd)/D1ServerSrc $LOGGING ResImpl.ResourceManagerImpl $1 $2
elif [ $# -eq 1 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djava.rmi.server.codebase=$(pwd)/D1ServerSrc ResImpl.Middleware $1 
fi 
