#!/bin/bash

echo param1=$1
echo param2=$2
echo param3=$3

args=$(cat "$1")
command=$2
password=$3

echo args=$args
echo command=$command
echo password=$password

echo -e "$password\n" | sudo --preserve-env --set-home --stdin $args $command
#sudo --preserve-env -set-home --stdin $args #scommand
