#include "stdafx.h"
#include <iostream>
#include "CommanLineParser.h"
#include "ProcessUnderService.h"
#include "Settings.h"
#include "HelpUtilities.h"
#include "ProcessInfoProvider.h"
#include "ProcessWithLogon.h"

class ProcessInfoProvider;
class ProcessUnderService;

int _tmain(int argc, _TCHAR *argv[]) {
	HelpUtilities::ShowHeader();

	Settings settings;
	CommanLineParser commanLineParser;
	if (!commanLineParser.TryParse(argc, argv, settings))
	{
		HelpUtilities::ShowHelp();
		return IProcess::ErrorExitCode;
	}

	std::wcout << "Starting: " << settings.GetCommandLine() << std::endl;
	std::wcout << "in directory: " << settings.GetWorkingDirectory() << std::endl;

	ProcessInfoProvider processInfoProvider;
	if (processInfoProvider.IsServiceProcess())
	{
		ProcessUnderService process;
		return process.Run(settings);
	}
	
	ProcessWithLogon process;
	return process.Run(settings);
}
