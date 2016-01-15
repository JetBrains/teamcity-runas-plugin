#include "stdafx.h"
#include "HelpUtilities.h"
#include <ostream>
#include <sstream>
#include "version.h"

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
	help << std::endl << L"To use this tool under the windows service the following requirements must be met:";
	help << std::endl << L"- The current user should have administrative privileges.";
	help << std::endl << L"- The service's account should have the ability to adjust memory quotas for a process and the ability to replace a process level token.";
	help << std::endl << L"  These privileges can be added via \"Control Panel\\Administrative Tools\\Local Security Policy\\User Rights Assignment\".";
	help << std::endl << L"  See https://technet.microsoft.com/en-us/library/cc758168(v=ws.10).aspx";
	help << std::endl << L"  The changes don't take effect until the next login.";
	help << std::endl;
	help << std::endl << L"Usage:";
	help << std::endl << L"JetBrains.runAs.exe configuration_arguments";
	help << std::endl << L"where configuration_arguments is a set:";
	help << std::endl << L"\t/u:user_name\t\t- \"user\" or \"domain\\user\" or \"user@domain\", required";
	help << std::endl << L"\t/p:password\t\t- user's password";
	help << std::endl << L"\t/e:executable\t\t- executable file, required";
	help << std::endl << L"\t/w:working_directory\t- (optional) current directory";
	help << std::endl << L"\t/b:exit_code_base\t- base number for exit code, -200 by default";
	help << std::endl << L"\t/c:configuration_file\t- (optional) text file, containing the any configuration arguments";	
	help << std::endl << L"\t/a:command_line_args\t- (optional) command line arguments, importantly: it should be a last argument";
	help << std::endl;
	help << std::endl << L"Example:";
	help << std::endl << L"JetBrains.runAs.exe /c:MyConfig.txt /p:MyPassword";
	help << std::endl << L"where \"MyConfig.txt\" is a text file containing following lines:";
	help << std::endl << L"\t/u:SomeDomain\\SomeUserName";
	help << std::endl << L"\t/e:WhoAmI.exe";
	help << std::endl << L"\t/a:/ALL";
	help << std::endl << L"\t/FO";
	help << std::endl << L"\tLIST";
	return help.str();
}