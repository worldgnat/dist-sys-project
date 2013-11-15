#!/bin/bash
username="pdavou"
oldifs=$IFS
IFS=$'\n'
for i in $(cat $1); do
	host=$(echo $i | awk '{print $1}')
	echo "Connecting to $host"
	ssh -t $username'@'$host screen -r
done
IFS=$oldifs
