#!/bin/bash

#  runAs (settings_file_name, command, bitness, password)
if [ $# -eq 4 ];
then
	eval "argsFile="$1""
	args=$(cat "$argsFile")
	eval "command="$2""
	eval "password="$4""

	#echo command="$command"

	# if root
	if [[ "$EUID" -eq 0 ]];
	then
		su -c "\"$command\"" "$args"
		exit $?
	fi

	# if not root
	"${0}" runAs "$args" "$command" "$password" arg5
	exit $?
fi

#  runAs (runAs, args, command, password, arg5)
if [ $# -eq 5 ];
then
	if [ "$1" = "runAs" ];
	then
		args="$2"
		command="$3"
		password="$4"

		tmpFile=$(tempfile)
		chmod a+rw "$tmpFile"

		cmd="'${0}' su '$tmpFile' '$command' '$args' arg5"
		eval "export -- SOCAT_CMD=\"$cmd\""

		# run command
		(
			# wait for password input
			attempts=50
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
			while ps axg | grep "$tmpFile" | grep -v "grep" > /dev/null;
			do
				sleep .1
			done
		) | (
			# su
			socat - $'EXEC:"bash -exec +x \'eval $SOCAT_CMD\'",pty,ctty,setsid'
		) 2> >(tee > "$tmpFile" >(grep -v "[Pp]assword:" >&2))

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

	# su (su, tmp_file, command, args, arg5)
	if [ "$1" = "su" ];
	then
		tmpFile="$2"
		command="$3"
		args="$4"

		su -c "\"$command\"" "$args"
		exitCode=$?

		echo -e "$exitCode\n" > "$tmpFile"
		exit $exitCode
	fi
fi

echo Invalid arguments. >&2
echo Usage: runAs.sh settings_file_name command_file_name password >&2
exit 255