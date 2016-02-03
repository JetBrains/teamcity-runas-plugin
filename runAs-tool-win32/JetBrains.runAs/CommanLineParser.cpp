#include "stdafx.h"
#include "CommanLineParser.h"
#include <fstream>
#include <sstream>
#include <regex>
#include "Settings.h"
#include <list>
#include "Result.h"
#include "ErrorCode.h"
#include "ExitCode.h"
#include "Args.h"

static const std::wregex ArgRegex = std::wregex(L"\\s*-\\s*(\\w+)\\s*:\\s*(.+)\\s*$");
static const std::wregex UserRegex = std::wregex(L"^([^@\\\\]+)@([^@\\\\]+)$|^([^@\\\\]+)\\\\([^@\\\\]+)$|(^[^@\\\\]+$)");

CommanLineParser::CommanLineParser()
{
}

Result<Settings> CommanLineParser::TryParse(std::list<std::wstring> args, ExitCode* exitCodeBase) const
{
	auto actualArgs = std::list<std::wstring>(args);

	std::wstring userName;
	std::wstring domain;
	std::wstring password;
	std::wstring executable;
	std::wstring workingDirectory;
	*exitCodeBase = DEFAULT_EXIT_CODE_BASE;
	std::wstringstream commandLineArgs;
	auto argsMode = 0; // 0 - gets tool args, 1 - gets executable, 2 - gets cmd args
	
	while (actualArgs.size() > 0)
	{
		auto arg = *actualArgs.begin();		

		if (argsMode == 2)
		{
			commandLineArgs << " ";
			commandLineArgs << NormalizeCmdArg(arg);
			actualArgs.erase(actualArgs.begin());
			continue;
		}

		if (argsMode == 1)
		{
			executable = arg;
			argsMode = 2;
			actualArgs.erase(actualArgs.begin());
			continue;
		}		

		std::wsmatch matchResult;
		if (!regex_search(arg, matchResult, ArgRegex))
		{
			argsMode = 1;
			continue;
		}

		actualArgs.erase(actualArgs.begin());

		auto argName = matchResult._At(1).str();
		auto argValue = matchResult._At(2).str();

		std::wstring argNameInLowCase;
		argNameInLowCase.resize(argName.size());
		transform(argName.begin(), argName.end(), argNameInLowCase.begin(), tolower);

		// User name like SomeUserName or domain\SomeUserName or SomeUserName@domain
		if (argNameInLowCase == L"u")
		{
			if (!regex_search(argValue, matchResult, UserRegex))
			{
				return Result<Settings>(ERROR_CODE_INVALID_USAGE, L"Invalid format of user name \"" + argValue + L"\"");
			}

			userName = matchResult._At(1).str();
			if (userName == L"")
			{
				userName = matchResult._At(4).str();
			}

			if (userName == L"")
			{
				userName = matchResult._At(5).str();
			}

			domain = matchResult._At(2).str();
			if (domain == L"")
			{
				domain = matchResult._At(3).str();
			}

			if (domain == L"")
			{
				domain = L".";
			}

			continue;
		}

		// Password
		if (argNameInLowCase == L"p")
		{
			password = argValue;
			continue;
		}		

		// Working directory
		if (argNameInLowCase == L"w")
		{
			workingDirectory = argValue;
			continue;
		}

		// Exit code base
		if (argNameInLowCase == L"b")
		{
			*exitCodeBase = stoi(argValue);
			continue;
		}		

		// Configuration file
		if (argNameInLowCase == L"c")
		{
			// Extract args from file
			auto configFileName = argValue;
			std::wifstream configFile;
			configFile.open(configFileName);
			if (!configFile.is_open())
			{
				return Result<Settings>(ERROR_CODE_INVALID_USAGE, L"Unable to open file: \"" + configFileName + L"\"");
			}

			std::wstring line;
			auto curElement = actualArgs.begin();
			while (getline(configFile, line))
			{
				curElement = actualArgs.insert(curElement, line);
				++curElement;
			}

			configFile.close();
			continue;
		}

		return Result<Settings>(ERROR_CODE_INVALID_USAGE, L"Invalid argument \"" + argName + L"\"");
	}	

	if (workingDirectory == L"")
	{
		TCHAR path[MAX_PATH];
		GetCurrentDirectory(MAX_PATH, path);
		workingDirectory = std::wstring(path);
	}

	std::list<std::wstring> emptyArgs;
	if (userName == L"")
	{		
		emptyArgs.push_back(ARG_USER_NAME);
	}	

	if (executable == L"")
	{
		emptyArgs.push_back(ARG_EXECUTABLE);		
	}

	if (emptyArgs.size() > 0)
	{
		std::wstringstream details;
		details << L"The argument(s):";
		for (auto emptyArgsIterrator = emptyArgs.begin(); emptyArgsIterrator != emptyArgs.end(); ++emptyArgsIterrator)
		{
			details << L" \"" << *emptyArgsIterrator << L"\"";
		}

		details << L" should not be empty.";
		return Result<Settings>(ERROR_CODE_INVALID_USAGE, details.str());
	}	

	return Settings(userName, domain, password, executable, workingDirectory, *exitCodeBase, commandLineArgs.str());
}

std::wstring CommanLineParser::NormalizeCmdArg(std::wstring cmdArg)
{	
	if(cmdArg.find(L' ') != std::string::npos && cmdArg.size() > 0 && !(cmdArg[0] == L'\"' && cmdArg[cmdArg.size() - 1] == L'\"'))
	{		
		return L'\"' + cmdArg + L'\"';
	}

	return cmdArg;
}