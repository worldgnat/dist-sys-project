#!/bin/bash

export CLASSPATH=$(pwd)/D1ServerSrc:$(pwd)/jgroups-3.4.1.Final.jar
killall -u $(whoami) rmiregistry
IP=$(ifconfig | grep eth0 -A 10 | grep "inet addr:" | sed -e 's/[^:]*:\([^ ]*\).*/\1/')
#LOGGING="-Djava.util.logging.config.file=logger.properties"
LOGGING=""
rmiregistry $1 &
if [ $# -eq 2 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Djava.rmi.server.codebase=$(pwd)/D1ServerSrc $LOGGING ResImpl.ResourceManagerImpl $1 $2
elif [ $# -eq 4 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djaja.rmi.server.codebase=$(pwd)/D1ServerSrc ResImpl.Middleware $1 $2 $3 $4
fi 
