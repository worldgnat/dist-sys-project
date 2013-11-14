#!/bin/bash

export CLASSPATH=$(pwd)/D1ClientSrc:$(pwd)/D1ClientSrc/ResInterface.jar:$(pwd)/D1ClientSrc/exceptions.jar
if [ $# -eq 2 ]; then
	java -Djava.security.policy=client.policy -Djava.rmi.server.codebase=file:$(pwd)/D1ClientSrc clientTest $1 $2
else 
	java -Djava.security.policy=client.policy -Djava.rmi.server.codebase=file:$(pwd)/D1ClientSrc clientTest
fi
