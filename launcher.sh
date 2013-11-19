#!/bin/bash

export CLASSPATH=$(pwd)/D1ServerSrc
killall -u $(whoami) rmiregistry
rmiregistry $1 &
if [ $# -eq 2 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djava.rmi.server.codebase=$(pwd)/D1ServerSrc ResImpl.ResourceManagerImpl $1 $2
elif [ $# -eq 4 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djaja.rmi.server.codebase=$(pwd)/D1ServerSrc ResImpl.Middleware $1 $2 $3 $4
fi 
