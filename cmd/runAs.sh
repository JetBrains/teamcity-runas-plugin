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

        # run command
        (
		# wait for password input
		attempts=100
		while [[ ! -s $exitCodeFile && attemps -le 0 ]];
		do
			sleep .1;
			attempts=$((attempts-1));
		done

		# send password to su stdIn
		echo "$password"

		# wait for process finish
		while ps axg | grep "su $exitCodeFile" > /dev/null; 
		do
			sleep .1;
		done
	) | (
		# su
		socat - EXEC:"$cmd",pty,ctty,setsid
	) 2> >(tee >"$exitCodeFile" >(grep -v "[Pp]assword:" >&2))

        # if exit file is empty
        if [ ! -s $exitCodeFile ];
        then
                echo "System or authentication failure" >&2
                exit 1
        fi

	# take exid code from file
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
