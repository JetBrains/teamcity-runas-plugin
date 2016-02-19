Feature: Inhetrit environment variables

Scenario: User runs the command whith the inherited environment by default
	Given I have appended the file command.cmd by the line @echo TestEnvVar=%TestEnvVar%
	And I've defined the TestEnvVar environment variable by the value TestValue
	And I've added the argument -u:TestUser
	And I've added the argument -p:aaa
	And I've added the argument command.cmd
	When I run RunAs tool
	Then the exit code should be 0
	And the output should contain:
	|                      |
	| TestEnvVar=TestValue |

Scenario Outline: User runs the command whith the inherited environment
	Given I have appended the file command.cmd by the line @echo TestEnvVar=%TestEnvVar%
	And I've defined the TestEnvVar environment variable by the value TestValue
	And I've added the argument -u:TestUser
	And I've added the argument -p:aaa
	And I've added the argument -i:<inhetritEnvironment>
	And I've added the argument command.cmd
	When I run RunAs tool
	Then the exit code should be 0
	And the output should contain:
	|                      |
	| TestEnvVar=TestValue |

Examples:
	| inhetritEnvironment |
	| ON                  |
	| ON                  |
	| ON                  |

Scenario Outline: User runs the command whith the not inherited environment
	Given I have appended the file command.cmd by the line @echo TestEnvVar=%TestEnvVar%
	And I've defined the TestEnvVar environment variable by the value TestValue
	And I've added the argument -u:TestUser
	And I've added the argument -p:aaa
	And I've added the argument -i:<inhetritEnvironment>
	And I've added the argument command.cmd
	When I run RunAs tool
	Then the exit code should be 0
	And the output should contain:
	|             |
	| TestEnvVar= |

Examples:
	| inhetritEnvironment |
	| OFF                 |
	| OFF                 |
	| OFF                 |