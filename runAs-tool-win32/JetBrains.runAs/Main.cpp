#include "stdafx.h"
#include "CommanLineParser.h"
#include "Settings.h"
#include "HelpUtilities.h"
#include "ErrorCode.h"
#include "Result.h"
#include <iostream>
#include "ProcessRunner.h"
#include "Args.h"
#include "Trace.h"

wstring GetStringValue(wstring value)
{
	if (value.size() == 0)
	{
		return L"empty";
	}

	return value;
}

int _tmain(int argc, _TCHAR *argv[]) {
	SetErrorMode(SEM_FAILCRITICALERRORS | SEM_NOGPFAULTERRORBOX | SEM_NOALIGNMENTFAULTEXCEPT | SEM_NOOPENFILEERRORBOX);
	
	list<wstring> args;
	for (auto argIndex = 1; argIndex < argc; argIndex++)
	{
		args.push_back(argv[argIndex]);
	}

	auto result = Result<ExitCode>();
	Settings settings;
	ExitCode exitCodeBase = DEFAULT_EXIT_CODE_BASE;
	LogLevel logLevel;
	try
	{
		CommanLineParser commanLineParser;
		auto settingsResult = commanLineParser.TryParse(args, &exitCodeBase, &logLevel);
		if (settingsResult.HasError())
		{
			result = Result<ExitCode>(settingsResult.GetErrorCode(), settingsResult.GetErrorDescription());
		}
		else
		{
			settings = settingsResult.GetResultValue();
		}
		
		if (!result.HasError())
		{		
			ProcessRunner runner;
			result = runner.Run(settings);			
		}
	}
	catch(...)
	{
		result = Result<ExitCode>(ERROR_CODE_UNKOWN, L"Unknown error");
	}

	Trace trace(settings.GetLogLevel());
	if (!result.HasError())
	{
		trace < L"Exit code: ";
		trace << result.GetResultValue();
		return result.GetResultValue();
	}

	if (logLevel != LOG_LEVEL_OFF && logLevel != LOG_LEVEL_ERRORS)
	{
		// Show header
		wcout << HelpUtilities::GetHeader();
	}
	
	if (logLevel != LOG_LEVEL_OFF && logLevel != LOG_LEVEL_ERRORS)
	{
		// Show arguments
		wcout << endl << endl << L"Argument(s):";
		if (args.size() == 0)
		{
			wcout << L" empty";
		}
		else
		{
			for (auto argsIterrator = args.begin(); argsIterrator != args.end(); ++argsIterrator)
			{
				wcout << L" " << *argsIterrator;
			}
		}
	}

	if (logLevel != LOG_LEVEL_OFF && logLevel != LOG_LEVEL_ERRORS)
	{
		// Show settings
		wcout << endl << endl << L"Settings:";
		wcout << endl << L"\t" << ARG_USER_NAME << L":\t\t" << GetStringValue(settings.GetUserName());
		wcout << endl << L"\t" << ARG_DOMAIN << L":\t\t\t" << GetStringValue(settings.GetDomain());
		wcout << endl << L"\t" << ARG_WORKING_DIRECTORY << L":\t" << GetStringValue(settings.GetWorkingDirectory());
		wcout << endl << L"\t" << ARG_EXIT_CODE_BASE << L":\t\t" << settings.GetExitCodeBase();
		wcout << endl << L"\t" << ARG_LOG_LEVEL << L":\t\t" << logLevel;
		wcout << endl << L"\t" << ARG_INHERITANCE_MODE << L":\t" << settings.GetInheritanceMode();
		wcout << endl << L"\t" << ARG_EXECUTABLE << L":\t\t" << GetStringValue(settings.GetExecutable());
		wcout << endl << L"\t" << ARG_EXIT_COMMAND_LINE_ARGS << L":\t" << GetStringValue(settings.GetCommandLine());

		if (result.GetErrorCode() == ERROR_CODE_INVALID_USAGE)
		{
			wcout << endl << HelpUtilities::GetHelp();
		}
	}

	if (logLevel != LOG_LEVEL_OFF)
	{
		if (result.GetErrorDescription() != L"")
		{
			wcerr << endl << endl << L"Error: " + result.GetErrorDescription();
		}
	}	

	auto exitCode = exitCodeBase > 0 ? exitCodeBase + result.GetErrorCode() : exitCodeBase - result.GetErrorCode();
	trace << L"Error code:" + exitCode;
	return exitCode;
}
