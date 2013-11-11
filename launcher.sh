#!/bin/bash
if [ $# -ge 2 ]; then 
	export CLASSPATH=$(pwd)/D1ServerSrc
	rmiregistry $1
	java -Djava.security.policy=D1ServerSrc/server.policy -Djava.rmi.codebase=$(pwd)/D1ServerSrc ResImpl.ResourceManager $1 $2
fi
