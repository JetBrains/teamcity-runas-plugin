#!/bin/bash

#  runAs (settings_file_name, command_file_name, password)
if [ $# -eq 3 ];
then
        args=$(cat "$1")
        command=$2
        password=$3
	
	exitCodeFile=$(tempfile)
        chmod a+w $exitCodeFile
	cmd="${0} su $exitCodeFile $command $args"

        started=0
        # run command
        (
		#sleep 1
		#wait for password input
		while [ ! -s $exitCodeFile ];
		do
			sleep .1;
		done
		echo "$password"

		#wait for process finish
		while ps axg | grep "$exitCodeFile" > /dev/null; 
		do 
			sleep .1;
		done
	) | (
		socat - EXEC:"$cmd",pty,ctty
	) 2> >(tee >"$exitCodeFile" >(grep -v "[Pp]assword:" >&2))
	
	#2> >(grep -v "[Pp]assword:")

        # if exit file is empty
        if [ ! -s $exitCodeFile ];
        then
                echo "System or authentication failure" >&2
                exit 255
        fi
	
	exitCode=$(cat "$exitCodeFile")
        rm $exitCodeFile 1> /dev/null 2> /dev/null
        exit $exitCode
fi

# su (su, exit_code_file, command_file_name, args)
if [ $# -eq 4 ];
then
	exitCodeFile=$2
	command=$3
	args=$4
	
	su -p -c "$command" "$args"
	exitCode=$?

	echo $exitCode>$exitCodeFile
	exit $exitCode
fi

echo Invalid arguments. >&2
echo Usage: runAs.sh settings_file_name command_file_name password >&2
exit 255
