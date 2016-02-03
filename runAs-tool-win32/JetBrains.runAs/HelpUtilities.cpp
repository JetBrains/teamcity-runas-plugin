#include "stdafx.h"
#include "HelpUtilities.h"
#include <ostream>
#include <sstream>
#include "version.h"
#include "Args.h"

HelpUtilities::HelpUtilities()
{
}

std::wstring HelpUtilities::GetHeader()
{	
	std::wstringstream header;
	header << VER_PRODUCTNAME_STR;
#if defined(_M_X64) || defined(x86_64)
	header << L" x64";
#else
	header << L" x86";
#endif
	header << L" " << VER_FILE_VERSION_STR;

	header << std::endl << VER_COPYRIGHT_STR;
	header << std::endl << VER_FILE_DESCRIPTION_STR;
	return header.str();
}

std::wstring HelpUtilities::GetHelp()
{	
	std::wstringstream help;
	help << std::endl << L"Important Notice:";
	help << std::endl << L"To use this tool under a windows service the following requirements must be met:";
	help << std::endl << L"- The service's account should have administrative privileges.";
	help << std::endl << L"- The service's account should have the ability to adjust memory quotas for a process and the ability to replace a process level token.";
	help << std::endl << L"  These privileges can be added via \"Control Panel\\Administrative Tools\\Local Security Policy\\User Rights Assignment\".";
	help << std::endl << L"  See https://technet.microsoft.com/en-us/library/cc758168(v=ws.10).aspx";
	help << std::endl << L"  The changes don't take effect until the next login.";
	help << std::endl;
	help << std::endl << L"Usage:";
	help << std::endl << L"JetBrains.runAs.exe arguments";
	help << std::endl << L"where arguments is a set:";
	help << std::endl << L"\t-u:" << ARG_USER_NAME << "\t\t- \"user\" or \"domain\\user\" or \"user@domain\"";
	help << std::endl << L"\t-p:" << ARG_PASSWORD << "\t\t- user's password, it is optional and empty by default";
	help << std::endl << L"\t-w:" << ARG_WORKING_DIRECTORY << "\t- current directory, it is optional and empty by default";
	help << std::endl << L"\t-b:" << ARG_EXIT_CODE_BASE << "\t- base number for exit code, it is optional and -200 by default";
	help << std::endl << L"\t-c:" << ARG_CONFIGURATION_FILE << L"\t- text file, containing the any configuration arguments, it is optional";
	help << std::endl << L"\t" << ARG_EXECUTABLE << "\t\t- executable file";
	help << std::endl << L"\t" << ARG_EXIT_COMMAND_LINE_ARGS << "\t- command line arguments, it is optional and empty by default ";
	help << std::endl;
	help << std::endl << L"Example:";
	help << std::endl << L"JetBrains.runAs.exe -p:MyPassword -c:MyConfig.txt";
	help << std::endl << L"where \"MyConfig.txt\" is a text file containing following lines:";
	help << std::endl << L"\t-u:SomeDomain\\SomeUserName";
	help << std::endl << L"\tWhoAmI.exe";
	help << std::endl << L"\t/ALL";
	help << std::endl << L"\t/FO";
	help << std::endl << L"\tLIST";
	return help.str();
}