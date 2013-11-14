#!/bin/bash

sed -i'' -e "s|file:[^\"]*|file:$(pwd)/$1|" $1/*.policy
