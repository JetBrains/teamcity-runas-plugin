Feature: Show log info

Scenario: User sees header when in the debug log mode without error
	Given I have appended the file command.cmd by the line echo
	And I've added the argument -u:RunAsTestUser
	And I've added the argument -p:aaa
	And I've added the argument -l:debug
	And I've added the argument command.cmd
	When I run RunAs tool
	Then the exit code should be 0
	And the output should contain:
	|                 |
	| JetBrains RunAs |	

Scenario: User sees header and settings info when in the debug log mode and error
	Given I have appended the file command.cmd by the line echo
	And I've added the argument -u:SomeAsTestUser
	And I've added the argument -p:SomePassword
	And I've added the argument -l:debug
	And I've added the argument command.cmd
	When I run RunAs tool
	Then the exit code should be 0
	And the output should contain:
	|                 |
	| JetBrains RunAs |
	| Settings:       |	
