#!/bin/bash

export CLASSPATH=$(pwd)/D1ServerSrc
rmiregistry $1 &
if [ $# -ge 2 ]; then 
	java -Djava.security.policy=D1ServerSrc/server.policy -Djava.rmi.server.codebase=$(pwd)/D1ServerSrc ResImpl.ResourceManagerImpl $1 $2
else
	java -Djava.security.policy=D1ServerSrc/server.policy -Djaja.rmi.server.codebase=$(pwd)/D1ServerSrc ResImpl.Middleware localhost $1
fi 
