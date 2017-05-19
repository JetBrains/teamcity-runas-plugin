#!/bin/bash

# runAs (settings_file_name, command, bitness, password)
if [ $# -eq 4 ];
then
	eval "argsFile="$1""
	args=$(cat "$argsFile")
	eval "command="$2""
	eval "password="$4""

	#echo command="$command"
	#echo password="$password"

	# if root
	if [[ "$EUID" -eq 0 ]];
	then
		# auth
		su -c "\"${0}\" auth \"$password\"" "$args"
		authCode=$?

		if [ "$authCode" = "0" ];
		then
			# su
			su -c "\"$command\"" "$args"
			exit $?
		else
			# Permission problem
			echo 'Incorrect user or password' >&2
			exit 126
		fi
	fi

	# if not root
	"${0}" runAs "$args" "$command" "$password" arg5
	exit $?
fi

# runAs(auth, password)
if [ $# -eq 2 ];
then
	if [ "$1" = "auth" ];
	then
		password="$2"

		tmpFile1=$(tempfile)
		sudo -k
		#sudo -lS &> /dev/null << EOF
		sudo -lS &> "$tmpFile1" << EOF
$password
EOF

		authCode=$?
		if [ $authCode -eq 0 ];
		then
			"$tmpFile1" &> /dev/null
			exit 0
		fi

		tmpFile2=$(tempfile)
		sudo -k
		#sudo -lS &> /dev/null << EOF
		sudo -lS &> "$tmpFile2" << EOF
solt$password
EOF

		if cmp -s "$tmpFile1" "$tmpFile2"
		then
			authCode=126
		else
			authCode=0
		fi

		rm "$tmpFile1" &> /dev/null
		rm "$tmpFile2" &> /dev/null

		exit $authCode
	fi
fi

# runAs (runAs, args, command, password, arg5)
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