#!/bin/bash

#  runAs (settings_file_name, command_file_name, password)
if [ $# -eq 3 ];
then
        args=$(cat "$1")
        command=$2
        password=$3

	tmpFile=$(tempfile)
        chmod a+rw "$tmpFile"
	cmd="${0} runAs $tmpFile $command $args"

        # run command
	if [[ "$EUID" -eq 0 ]];
        then
		# if root
                $cmd
	else
	        (
			# wait for password input
			attempts=100
			while [[ ! -s "$tmpFile" || attemps -gt 0 ]];
			do
				sleep .1
				attempts=$((attempts-1))
			done

			if [[ $attempts -eq 0 ]];
			then
				echo "##teamcity[message text='Error during sending password.' status='ERROR']"
				exit 255
			fi

			# send password to su stdIn
			echo "$password"

			# wait for process finish
			while ps axg | grep "$tmpFile" > /dev/null; 
			do
				sleep .1
			done
		) | (
			# su
			socat - EXEC:"$cmd",pty,ctty,setsid;
		) 2> >(tee > "$tmpFile" >(grep -v "[Pp]assword:" >&2))
	fi

        # if exit file is empty
        if [ ! -s "$tmpFile" ];
        then
		exit 255
        fi

	# take exid code from file
	exitCode=$(cat "$tmpFile")
	rm "$tmpFile" 1> /dev/null 2> /dev/null
	exit $exitCode
fi

# su (runAs, tmp_file, command_file_name, args)
if [ $# -eq 4 ];
then
	tmpFile=$2
	command=$3
	args=$4

	su -p -c "$command" "$args"
	exitCode=$?

	echo -e "$exitCode\n" > "$tmpFile"
	exit $exitCode
fi

echo Invalid arguments. >&2
echo Usage: runAs.sh settings_file_name command_file_name password >&2
exit 255
