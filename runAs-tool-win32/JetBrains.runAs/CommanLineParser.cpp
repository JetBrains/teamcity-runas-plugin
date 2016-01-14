#include "stdafx.h"
#include "CommanLineParser.h"
#include <iostream>
#include <fstream>
#include <regex>
#include "Settings.h"
#include <list>
#include <codecvt>

static const std::wregex ArgRegex = std::wregex(L"\\s*/\\s*(\\w)+\\s*:\\s*(.+)\\s*$");
static const std::wregex UserRegex = std::wregex(L"^([^@\\\\]+)@([^@\\\\]+)$|^([^@\\\\]+)\\\\([^@\\\\]+)$|(^[^@\\\\]+$)");

CommanLineParser::CommanLineParser()
{
}

bool CommanLineParser::TryParse(int argc, _TCHAR *argv[],  Settings& settings) const
{	
	if (argc < 2)
	{
		return false;
	}	

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
	std::list<std::wstring> commandLineArgs;
	
	while (args.size() > 0)
	{
		auto arg = *args.begin();
		args.erase(args.begin());

		std::wsmatch matchResult;
		if (!regex_search(arg, matchResult, ArgRegex))
		{
			std::wcerr << "Invalid argument \"" << arg << "\"";
			return false;
		}

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
				std::wcerr << "Invalid format of user name \"" << argValue << "\"";
				return false;
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

			continue;
		}

		// Password
		if (argNameInLowCase == L"p")
		{
			password = argValue;
			continue;
		}

		// Executable file
		if (argNameInLowCase == L"e")
		{
			executable = argValue;
			continue;
		}

		// Working directory
		if (argNameInLowCase == L"w")
		{
			workingDirectory = argValue;
			continue;
		}

		// Command line argument
		if (argNameInLowCase == L"a")
		{
			commandLineArgs.push_back(argValue);
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
				std::wcerr << std::endl << "Unable to open file: \"" << configFileName << "\"";
				return false;
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

		std::wcerr << "Invalid argument \"" << argName << "\"";
		return false;
	}	

	if (workingDirectory == L"")
	{
		TCHAR path[MAX_PATH];
		GetCurrentDirectory(MAX_PATH, path);
		workingDirectory = std::wstring(path);
	}

	settings = Settings(userName, domain, password, executable, workingDirectory, commandLineArgs);
	return true;
}

std::wstring CommanLineParser::ToWString(const std::string& text)
{
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	return converter.from_bytes(text);
}