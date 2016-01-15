Feature: RunAs checks command line argument and shows help

Scenario: RunAs returns -201 exit code and shows error message when user runs without user name arg
	Given I have appended the file command.cmd by the line WhoAmI.exe
	And I've added the argument -p:aaa
	And I've added the argument command.cmd
	When I run RunAs tool
	Then the exit code should be -201
	And the errors should contain:
	|                               |
	| Error:                        |
	| user_name should be specified |

Scenario: RunAs returns -201 exit code and shows error message when user runs without execurable arg
	Given I have appended the file command.cmd by the line WhoAmI.exe
	And I've added the argument -u:TestUser
	And I've added the argument -p:aaa
	When I run RunAs tool
	Then the exit code should be -201
	And the errors should contain:
	|                                |
	| Error:                         |
	| executable should be specified |

Scenario: RunAs returns -201 exit code and shows error message when user add cmd args not in the end via config file for args
	Given I have appended the file command.cmd by the line @echo %1 %2
	And I have appended the file args.txt by the line -p:aaa
	And I have appended the file args.txt by the line command.cmd
	And I have appended the file args.txt by the line hello
	And I have appended the file args.txt by the line "world !!!"
	And I've added the argument -c:args.txt	
	And I've added the argument -u:TestUser	
	When I run RunAs tool
	Then the exit code should be -201	

Scenario: RunAs returns -201 exit code and shows error message when user add cmd args not in the
	Given I have appended the file command.cmd by the line @echo %1 %2
	And I've added the argument -u:TestUser	
	And I've added the argument -c:args.txt	
	And I've added the argument hello
	And I've added the argument command.cmd
	And I've added the argument "world !!!"
	And I've added the argument -p:aaa
	When I run RunAs tool
	Then the exit code should be -201