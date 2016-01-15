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

static const std::wregex ArgRegex = std::wregex(L"\\s*-\\s*(\\w+)\\s*:\\s*(.+)\\s*$");
static const std::wregex UserRegex = std::wregex(L"^([^@\\\\]+)@([^@\\\\]+)$|^([^@\\\\]+)\\\\([^@\\\\]+)$|(^[^@\\\\]+$)");

CommanLineParser::CommanLineParser()
{
}

Result<bool> CommanLineParser::TryParse(int argc, _TCHAR *argv[],  Settings& settings) const
{	
	std::list<std::wstring> args;

	// Extract additional args from command line args
	for (auto argIndex = 1; argIndex < argc; argIndex++)
	{
		args.push_back(argv[argIndex]);
	}
	
	std::wstring userName;
	std::wstring domain;
	std::wstring password;
	std::wstring executable;
	std::wstring workingDirectory;
	ExitCode exitCodeBase = DEFAULT_EXIT_CODE_BASE;
	std::wstringstream commandLineArgs;
	auto argsMode = 0; // 0 - get tool args, 1 - get executable, 2 - get cmd args
	
	while (args.size() > 0)
	{
		auto arg = *args.begin();		

		if (argsMode == 2)
		{
			commandLineArgs << " ";
			commandLineArgs << NormalizeCmdArg(arg);
			args.erase(args.begin());
			continue;
		}

		if (argsMode == 1)
		{
			executable = arg;
			argsMode = 2;
			args.erase(args.begin());
			continue;
		}		

		std::wsmatch matchResult;
		if (!regex_search(arg, matchResult, ArgRegex))
		{
			argsMode = 1;
			continue;
		}

		args.erase(args.begin());

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
				return Result<bool>(ERROR_CODE_INVALID_USAGE, L"Invalid format of user name \"" + argValue + L"\"");
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
			exitCodeBase = stoi(argValue);
			continue;
		}

		// Command line argument
		if (argNameInLowCase == L"a")
		{
			argsMode = true;
			commandLineArgs << NormalizeCmdArg(argValue);
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
				return Result<bool>(ERROR_CODE_INVALID_USAGE, L"Unable to open file: \"" + configFileName + L"\"");
			}

			std::wstring line;
			auto curElement = args.begin();
			while (getline(configFile, line))
			{
				curElement = args.insert(curElement, line);
				++curElement;
			}

			configFile.close();
			continue;
		}

		return Result<bool>(ERROR_CODE_INVALID_USAGE, L"Invalid argument \"" + argName + L"\"");
	}	

	if (workingDirectory == L"")
	{
		TCHAR path[MAX_PATH];
		GetCurrentDirectory(MAX_PATH, path);
		workingDirectory = std::wstring(path);
	}

	std::wstringstream errors;
	if (userName == L"")
	{		
		errors << "user_name";
	}	

	if (executable == L"")
	{
		errors << " executable";
	}

	auto errorsDetails = errors.str();
	if (errorsDetails != L"")
	{		
		return Result<bool>(ERROR_CODE_INVALID_USAGE, L"The argument(s) " + errorsDetails + L" should be specified.");
	}

	settings = Settings(userName, domain, password, executable, workingDirectory, exitCodeBase, commandLineArgs.str());
	return Result<bool>(true);
}

std::wstring CommanLineParser::NormalizeCmdArg(std::wstring cmdArg)
{	
	if(cmdArg.find(L' ') != std::string::npos && cmdArg.size() > 0 && !(cmdArg[0] == L'\"' && cmdArg[cmdArg.size() - 1] == L'\"'))
	{		
		return L'\"' + cmdArg + L'\"';
	}

	return cmdArg;
}