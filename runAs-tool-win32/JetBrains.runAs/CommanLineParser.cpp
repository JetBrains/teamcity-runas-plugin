#include "stdafx.h"
#include "CommanLineParser.h"
#include <iostream>
#include <fstream>
#include <regex>
#include "Settings.h"
#include <list>
#include <codecvt>

CommanLineParser::CommanLineParser()
{
}

bool CommanLineParser::TryParse(int argc, char *argv[],  Settings& settings) const
{
	if (argc < 2)
	{
		return false;
	}	

	std::list<std::string> args;

	// Extract additional args from command line args
	for (auto argIndex = 1; argIndex < argc; argIndex++)
	{
		args.push_back(argv[argIndex]);
	}

	std::string userName;
	std::string domain;
	std::string password;
	std::string executable;
	std::string workingDirectory;
	std::list<std::wstring> commandLineArgs;
	
	while (args.size() > 0)
	{
		auto arg = *args.begin();
		args.erase(args.begin());

		std::smatch matchResult;
		if (!regex_search(arg, matchResult, argRegex))
		{
			std::cerr << "Invalid argument \"" << arg << "\"";
			return false;
		}

		auto argName = matchResult._At(1).str();
		auto argValue = matchResult._At(2).str();

		std::string argNameInLowCase;
		argNameInLowCase.resize(argName.size());
		transform(argName.begin(), argName.end(), argNameInLowCase.begin(), tolower);

		// User name like SomeUserName or domain\SomeUserName or SomeUserName@domain
		if (argNameInLowCase == "u")
		{
			if (!regex_search(argValue, matchResult, userRegex))
			{
				std::cerr << "Invalid format of user name \"" << argValue << "\"";
				return false;
			}

			userName = matchResult._At(1).str();
			if (userName == "")
			{
				userName = matchResult._At(4).str();
			}

			if (userName == "")
			{
				userName = matchResult._At(5).str();
			}

			domain = matchResult._At(2).str();
			if (domain == "")
			{
				domain = matchResult._At(3).str();
			}

			continue;
		}

		// Password
		if (argNameInLowCase == "p")
		{
			password = argValue;
			continue;
		}

		// Executable file
		if (argNameInLowCase == "e")
		{
			executable = argValue;
			continue;
		}

		// Working directory
		if (argNameInLowCase == "w")
		{
			workingDirectory = argValue;
			continue;
		}

		// Command line argument
		if (argNameInLowCase == "a")
		{
			commandLineArgs.push_back(ToWString(argValue));
			continue;
		}

		// Configuration file
		if (argNameInLowCase == "c")
		{
			// Extract args from file
			auto configFileName = argValue;
			std::ifstream configFile;
			configFile.open(configFileName);
			if (!configFile.is_open())
			{
				std::cerr << std::endl << "Unable to open file: \"" << configFileName << "\"";
				return false;
			}

			std::string line;
			auto curElement = args.begin();
			while (getline(configFile, line))
			{
				curElement = args.insert(curElement, line);
				++curElement;
			}

			configFile.close();
			continue;
		}

		std::cerr << "Invalid argument \"" << argName << "\"";
		return false;
	}	

	auto workingDirectoryW = ToWString(workingDirectory);
	if (workingDirectoryW == L"")
	{
		TCHAR path[MAX_PATH];
		GetCurrentDirectory(MAX_PATH, path);
		workingDirectoryW = std::wstring(path);
	}

	settings = Settings(ToWString(userName), ToWString(domain), ToWString(password), ToWString(executable), workingDirectoryW, commandLineArgs);
	return true;
}

std::wstring CommanLineParser::ToWString(const std::string& text)
{
	std::wstring_convert<std::codecvt_utf8_utf16<wchar_t>> converter;
	return converter.from_bytes(text);
}