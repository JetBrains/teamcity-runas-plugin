#!/bin/bash

user="testuser"
password="Catcat01"

if [ $# = "0" ]; then
    echo -e "$password\n" | sudo --preserve-env --set-home --user $user --stdin ./command.sh
fi