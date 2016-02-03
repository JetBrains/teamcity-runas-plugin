#include "stdafx.h"
#include "CommanLineParser.h"
#include "ProcessUnderService.h"
#include "Settings.h"
#include "HelpUtilities.h"
#include "ProcessInfoProvider.h"
#include "ProcessWithLogon.h"
#include "ErrorCode.h"
#include "Result.h"
#include <iostream>
#include "ProcessRunner.h"
#include "Args.h"

class ProcessInfoProvider;
class ProcessUnderService;

std::wstring GetStringValue(std::wstring value)
{
	if (value.size() == 0)
	{
		return L"empty";
	}

	return value;
}

int _tmain(int argc, _TCHAR *argv[]) {
	SetErrorMode(SEM_NOGPFAULTERRORBOX);
	
	std::list<std::wstring> args;
	for (auto argIndex = 1; argIndex < argc; argIndex++)
	{
		args.push_back(argv[argIndex]);
	}

	auto result = Result<ExitCode>();
	Settings settings;
	ExitCode exitCodeBase = DEFAULT_EXIT_CODE_BASE;
	try
	{
		ProcessInfoProvider processInfoProvider;
		CommanLineParser commanLineParser;

		auto settingsResult = commanLineParser.TryParse(args, &exitCodeBase);
		if (settingsResult.HasError())
		{
			result = Result<ExitCode>(settingsResult.GetErrorCode(), settingsResult.GetErrorDescription() + L"\n" + HelpUtilities::GetHelp());
		}
		else
		{
			settings = settingsResult.GetResultValue();
		}
		
		if (!result.HasError() && !processInfoProvider.IsSuitableOS())
		{
			result = Result<ExitCode>(ERROR_CODE_INVALID_USAGE, L"Use x64 version for the 64-bit OS.");			
		}		

		if (!result.HasError())
		{
			auto isServiceProcess = processInfoProvider.IsServiceProcess();
			if (isServiceProcess.HasError())
			{
				result = Result<ExitCode>(isServiceProcess.GetErrorCode(), isServiceProcess.GetErrorDescription());
			}
			else
			{
				if (isServiceProcess.GetResultValue())
				{
					auto isAdmin = processInfoProvider.IsUserAnAdministrator();
					if (isAdmin.HasError())
					{
						result = Result<ExitCode>(isAdmin.GetErrorCode(), isAdmin.GetErrorDescription());
					}
					else
					{
						if (!isAdmin.GetResultValue())
						{
							result = Result<ExitCode>(ERROR_CODE_ACCESS, L"The current user should have administrative privileges.");
						}
					}

					if (!result.HasError())
					{
						ProcessRunner<ProcessUnderService> runner;
						result = runner.Run(settings);
					}					
				}
				else
				{
					ProcessRunner<ProcessWithLogon> runner;
					result = runner.Run(settings);					
				}				
			}
		}
	}
	catch(...)
	{
		result = Result<ExitCode>(ERROR_CODE_UNKOWN, L"Unknown error");
	}

	if (!result.HasError())
	{
		return result.GetResultValue();
	}

	// Show header
	std::wcout << HelpUtilities::GetHeader();
	
	// Show arguments
	std::wcout << std::endl << std::endl << L"Argument(s):";
	if(args.size() == 0)
	{		
		std::wcout << L" empty";
	}
	else
	{
		for (auto argsIterrator = args.begin(); argsIterrator != args.end(); ++argsIterrator)
		{
			std::wcout << L" " << *argsIterrator;
		}
	}

	// Show settings
	std::wcout << std::endl << std::endl << L"Settings:";
	std::wcout << std::endl << L"\t" << ARG_USER_NAME << L":\t\t" << GetStringValue(settings.GetUserName());
	std::wcout << std::endl << L"\t" << ARG_DOMAIN << L":\t\t\t" << GetStringValue(settings.GetDomain());
	std::wcout << std::endl << L"\t" << ARG_WORKING_DIRECTORY << L":\t" << GetStringValue(settings.GetWorkingDirectory());
	std::wcout << std::endl << L"\t" << ARG_EXIT_CODE_BASE << L":\t\t" << settings.GetExitCodeBase();
	std::wcout << std::endl << L"\t" << ARG_EXECUTABLE << L":\t\t" << GetStringValue(settings.GetExecutable());
	std::wcout << std::endl << L"\t" << ARG_EXIT_COMMAND_LINE_ARGS << L":\t" << GetStringValue(settings.GetCommandLine());

	auto errorDescription = result.GetErrorDescription();
	if (errorDescription != L"")
	{
		std::wcerr << std::endl << std::endl << L"Error:" << std::endl << result.GetErrorDescription();
	}

	return exitCodeBase > 0 ? exitCodeBase + result.GetErrorCode() : exitCodeBase - result.GetErrorCode();
}
