#include "stdafx.h"
#include "HelpUtilities.h"
#include <ostream>
#include <iostream>

HelpUtilities::HelpUtilities()
{
}

void HelpUtilities::ShowHeader()
{
	std::cout << "JetBrains RunAs tool";
#if defined(_M_X64) || defined(x86_64)
	std::cout << " x64";
#else
	std::cout << " x86";
#endif

	std::cout << std::endl << "Copyright(C) JetBrains. All rights reserved.";
	std::cout << std::endl;
	std::cout << std::endl;
}

void HelpUtilities::ShowHelp()
{	
	std::cout << std::endl;
	std::cout << std::endl << "Requirements:";
	std::cout << std::endl << "\tTo run this tool the following requirements must be met:";
	std::cout << std::endl << "\t\t- Log on as a service under the account using administrative privileges.";
	std::cout << std::endl << "\t\t- This account has the privileges:";
	std::cout << std::endl << "\t\t\t- to act as part of the operating system";
	std::cout << std::endl << "\t\t\t- to adjust memory quotas for a process";
	std::cout << std::endl << "\t\t\t- to replace a process level token";
	std::cout << std::endl;
	std::cout << std::endl << "\t\t\tThese privileges can be added via Control Panel\\Administrative Tools\\Local Security Policy\\User Rights Assignment";
	std::cout << std::endl << "\t\t\tand they don't take effect until the next login.";
	std::cout << std::endl;
	std::cout << std::endl << "Usage:";
	std::cout << std::endl << "\tJetBrains.runAs.exe configuration_arguments";
	std::cout << std::endl << "\twhere configuration_arguments is a set:";
	std::cout << std::endl << "\t\t/u:userName\t\t- userName is \"user\" or \"domain\\user\" or \"user@domain\"";
	std::cout << std::endl << "\t\t/p:password";
	std::cout << std::endl << "\t\t/e:executable\t\t- executable file";
	std::cout << std::endl << "\t\t/w:working_directory\t- current directory if it is not specified";
	std::cout << std::endl << "\t\t/c:configuration_file\t- text file, containing the other configuration arguments";
	std::cout << std::endl << "\t\t/a:commandLineArg1";
	std::cout << std::endl << "\t\t/a:commandLineArg2";
	std::cout << std::endl << "\t\t...";
	std::cout << std::endl << "\t\t/a:commandLineArgN";
	std::cout << std::endl;
	std::cout << std::endl << "Example:";
	std::cout << std::endl << "\tJetBrains.runAs.exe /c:myconfig.txt /p:MyPassword";
	std::cout << std::endl << "\t\tThe text file \"myconfig.txt\" contains following lines:";
	std::cout << std::endl << "\t\t\t/u:myDomain\\SomeUser";
	std::cout << std::endl << "\t\t\t/e:WhoAmI.exe";
	std::cout << std::endl << "\t\t\t/a:/ALL";
	std::cout << std::endl << "\t\t\t/a:/FO";
	std::cout << std::endl << "\t\t\t/a:LIST";
}
