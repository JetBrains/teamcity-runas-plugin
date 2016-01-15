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

class ProcessInfoProvider;
class ProcessUnderService;

int _tmain(int argc, _TCHAR *argv[]) {
	SetErrorMode(SEM_NOGPFAULTERRORBOX);

	auto result = Result<ExitCode>();
	Settings settings;
	try
	{
		ProcessInfoProvider processInfoProvider;
		CommanLineParser commanLineParser;
		auto parsed = commanLineParser.TryParse(argc, argv, settings);
		if (parsed.HasError())
		{
			result = Result<ExitCode>(parsed.GetErrorCode(), parsed.GetErrorDescription() + L"\n" + HelpUtilities::GetHelp());
		}
		else
		{
			if (!parsed.GetResultValue())
			{
				result = Result<ExitCode>(ERROR_CODE_INVALID_USAGE, L"The arguments were not parsed.");
			}
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

	std::wcout << HelpUtilities::GetHeader();
	auto errorDescription = result.GetErrorDescription();
	if (errorDescription != L"")
	{
		std::wcerr << std::endl << std::endl << L"Error:" << std::endl << result.GetErrorDescription();
	}

	auto exitCodeBase = settings.GetExitCodeBase();
	return exitCodeBase > 0 ? exitCodeBase + result.GetErrorCode() : exitCodeBase - result.GetErrorCode();
}
