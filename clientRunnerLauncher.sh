#!/bin/bash

export CLASSPATH=$(pwd)/D1ClientSrc:$(pwd)/D1ClientSrc/ResInterface.jar:$(pwd)/D1ClientSrc/exceptions.jar
testpath="D1ServerSrc/ClientTests"
if [ $# -eq 2 ]; then
	java -Djava.security.policy=client.policy -Djava.rmi.server.codebase=file:$(pwd)/D1ClientSrc ClientRunner $1 $2 $testpath/cars $testpath/flights
fi
