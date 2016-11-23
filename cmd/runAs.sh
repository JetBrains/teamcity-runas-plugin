#!/bin/bash

args=$(cat "$1")
command=$2
password=$3

(sleep .3; echo "$password") | socat - EXEC:'su -c $command $args',pty,ctty,setsid