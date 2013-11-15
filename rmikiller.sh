#!/bin/bash
username="pdavou"
oldifs=$IFS
IFS=$'\n'
for i in $(cat $1); do
	ssh $username'@'$(echo $i | awk '{print $1}') killall -u $username rmiregistry
	echo 'Closed rmiregistry on ' $(echo $i | awk '{print $1}')
done
IFS=$oldifs
