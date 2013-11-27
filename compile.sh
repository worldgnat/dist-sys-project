#!/bin/bash

echo "Compiling Server"
cd D1ServerSrc
find . -type f | grep .java | sed -e 's/\.\///' | xargs javac -cp $(pwd):$(pwd)/../jgroups-3.4.1.Final.jar
jar cvf ResInterface.jar ResInterface/*.class
jar cvf exceptions.jar exceptions/*.class
cp *.jar ../D1ClientSrc/

echo "Compiling Client"
cd ../D1ClientSrc
find . -type f | grep .java | sed -e 's/\.\///' | xargs javac -cp $(pwd):$(pwd)/ResInterface.jar:$(pwd)/exceptions.jar
