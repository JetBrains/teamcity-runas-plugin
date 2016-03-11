#include "stdafx.h"
#include "CommanLineParser.h"
#include "Settings.h"
#include "HelpUtilities.h"
#include "ErrorCode.h"
#include "Result.h"
#include <iostream>
#include "ProcessRunner.h"
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
	Console console;
	wstring endl = L"\n\r";
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
			// Show header
			Trace trace(settings.GetLogLevel());
			trace < HelpUtilities::GeTitle();

			ProcessRunner runner;
			trace < L"main::Run starting";			
			result = runner.Run(settings);
			trace < L"main::Run finished";
		}
	}
	catch(...)
	{
		result = Result<ExitCode>(ERROR_CODE_UNKOWN, L"Unknown error");
	}	

	Trace trace(settings.GetLogLevel());
	trace < L"main::Create results";

	if (!result.HasError())
	{
		trace < L"Exit code: ";
		trace << result.GetResultValue();
		return result.GetResultValue();
	}

	if (logLevel != LOG_LEVEL_OFF && logLevel != LOG_LEVEL_ERRORS)
	{
		// Show header
		console << HelpUtilities::GetHeader();
	}
	
	if (logLevel != LOG_LEVEL_OFF && logLevel != LOG_LEVEL_ERRORS)
	{
		// Show arguments
		console << endl << endl << L"Argument(s):";
		if (args.size() == 0)
		{
			console << L" empty";
		}
		else
		{
			for (auto argsIterrator = args.begin(); argsIterrator != args.end(); ++argsIterrator)
			{
				console << L" " << *argsIterrator;
			}
		}
	}

	if (logLevel != LOG_LEVEL_OFF && logLevel != LOG_LEVEL_ERRORS)
	{
		// Show settings
		console << endl << endl << L"Settings:" << endl << settings.ToString();
		if (result.GetErrorCode() == ERROR_CODE_INVALID_USAGE)
		{
			console << endl << HelpUtilities::GetHelp();
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
