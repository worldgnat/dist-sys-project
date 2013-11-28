#!/bin/bash

export CLASSPATH=$(pwd)/D1ServerSrc:$(pwd)/jgroups-3.4.1.Final.jar
killall -u $(whoami) rmiregistry && sleep 1
IP=$(ifconfig | grep eth0 -A 10 | grep "inet addr:" | sed -e 's/[^:]*:\([^ ]*\).*/\1/')
#LOGGING="-Djava.util.logging.config.file=logger.properties"
JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=$IP -Djava.rmi.server.codebase=file:$(pwd)/D1ServerSrc"
rmiregistry $1 &
if [ $# -eq 2 ]; then 
#	if [ $2 == "cars29" ]; then
#		JAVA_OPTS=$JAVA_OPTS" -Djgroups.udp.mcast_port"
#	elif [ $2 == "rooms29" ]; then 
#		JAVA_OPTS=$JAVA_OPTS" -Djgroups.udp.mcast_port"
#	else
#		JAVA_OPTS=
	java -Djava.security.policy=D1ServerSrc/server.policy $JAVA_OPTS ResImpl.ResourceManagerImpl $1 $2
elif [ $# -eq 1 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy $JAVA_OPTS ResImpl.Middleware $1 
fi 
