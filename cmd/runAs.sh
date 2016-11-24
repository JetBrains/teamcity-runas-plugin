#!/bin/bash

#  run as user (settings_file_name, command_file_name, password)
if [ $# -eq 3 ];
then
        args=$(cat "$1")
        command=$2
        password=$3

        exitCodeFile=$(tempfile)
        chmod a+w $exitCodeFile

        cmd="su -c \\\"${0} $command $exitCodeFile \\\" $args"

        # run command
        (sleep .3; echo "$password") | socat - EXEC:"$cmd",pty,ctty,setsid 2> >(grep -v "[Pp]assword:")

        # if exit file is empty
        if [ ! -s $exitCodeFile ];
        then
                echo "System or authentication failure" >&2
                exit 255
        fi

        # read cmd exit code and return it
        exitCode=$(cat "$exitCodeFile")
        rm $exitCodeFile 1> /dev/null 2> /dev/null
        exit $exitCode
fi

# run command (command_file_name, exit_code_file)
if [ $# -eq 2 ];
then
        command=$1
        exitCodeFile=$2

        $command
        echo $?>$exitCodeFile
        exit 0          
fi


echo Invalid arguments. >&2
echo Usage: runAs.sh settings_file_name command_file_name password >&2
exit 255
