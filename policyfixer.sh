#!/bin/bash

sed -i'' -e "s|file:[^\"]*|file:$(pwd)/D1ServerSrc|" $1/*.policy
