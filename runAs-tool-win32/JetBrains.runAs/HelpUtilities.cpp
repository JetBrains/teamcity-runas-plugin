#include "stdafx.h"
#include "HelpUtilities.h"
#include <ostream>
#include <sstream>
#include "version.h"
#include "Args.h"
#include "LogLevel.h"
#include "ExitCode.h"
#include "InheritanceMode.h"
#include "IntegrityLevel.h"
#include "StringUtilities.h"
#include "ShowMode.h"

HelpUtilities::HelpUtilities()
{
}

wstring HelpUtilities::GeTitle()
{
	wstringstream header;
	header << VER_PRODUCTNAME_STR;
#if defined(_M_X64) || defined(x86_64)
	header << L" x64";
#else
	header << L" x86";
#endif
	header << L" " << VER_FILE_VERSION_STR;
	return header.str();
}

wstring HelpUtilities::GetHeader()
{	
	wstringstream header;
	header << GeTitle();
	header << endl << VER_COPYRIGHT_STR;
	header << endl << VER_FILE_DESCRIPTION_STR;
	return header.str();
}

wstring HelpUtilities::GetHelp()
{	
	wstringstream help;
	help << endl << L"Usage:";
	help << endl << L"JetBrains.runAs.exe arguments";
	help << endl << L"where arguments is a set:";
	help << endl << L"\t-u:" << ARG_USER_NAME << "\t\t- \"user\" or \"domain\\user\" or \"user@domain\".";
	help << endl << L"\t-p:" << ARG_PASSWORD << "\t\t- user's password, it is optional and empty by default.";
	help << endl << L"\t-w:" << ARG_WORKING_DIRECTORY << "\t- current directory, it is optional and empty by default.";
	help << endl << L"\t-b:" << ARG_EXIT_CODE_BASE << "\t- base number for exit code, it is optional and " << DEFAULT_EXIT_CODE_BASE << L" by default.";
	help << endl << L"\t-e:" << ARG_ENV_VAR << "\t- set an environment variable in the format \"name=value\".";
	help << endl << L"\t-l:" << ARG_LOG_LEVEL << "\t\t- logging level (" << StringUtilities::Join(LogLevels, L"|") << L"), it is optional and \"" << LOG_LEVEL_NORMAL << L"\" by default.";
	help << endl << L"\t-il:" << ARG_INTEGRITY_LEVEL << "\t- integrity level (" << StringUtilities::Join(IntegrityLevels, L"|") << L"), it is optional and \"" << INTEGRITY_LEVEL_AUTO << L"\" by default.";
	help << endl << L"\t-s:" << ARG_SHOW_MODE << "\t\t- show mode (" << StringUtilities::Join(ShowModes, L"|") << L"), it is optional and \"" << SHOW_MODE_HIDE << L"\" by default.";
	help << endl << L"\t-i:" << ARG_INHERITANCE_MODE << "\t- set \"" << INHERITANCE_MODE_ON << L"\" when the environment variables should be inherited from a parent process, set \"" << INHERITANCE_MODE_AUTO << L"\" when the some environment variables should be inherited from a parent process, set to \"" << INHERITANCE_MODE_OFF << L"\" when environment variables should not be inherited from a parent process, it is optional and \"" << INHERITANCE_MODE_AUTO << L"\" by default.";
	help << endl << L"\t-c:" << ARG_CONFIGURATION_FILE << L"\t- text file, containing the any configuration arguments, it is optional.";
	help << endl << L"\t" << ARG_EXECUTABLE << "\t\t- executable file.";
	help << endl << L"\t" << ARG_EXIT_COMMAND_LINE_ARGS << "\t- command line arguments, it is optional and empty by default. The maximum total length of \"" << ARG_EXECUTABLE << L"\" and \"" << ARG_EXIT_COMMAND_LINE_ARGS << L"\" is 1024 characters.";
	help << endl;
	help << endl << L"Example:";
	help << endl << L"JetBrains.runAs.exe -p:MyPassword -c:MyConfig.txt";
	help << endl << L"where \"MyConfig.txt\" is a text file containing following lines:";
	help << endl << L"\t-u:SomeDomain\\SomeUserName";
	help << endl << L"\tWhoAmI.exe";
	help << endl << L"\t/ALL";
	help << endl << L"\t/FO";
	help << endl << L"\tLIST";
	return help.str();
}
