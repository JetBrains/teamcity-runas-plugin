#include "stdafx.h"
#include <iostream>
#include "CommanLineParser.h"
#include "Process.h"
#include "Settings.h"
#include "HelpUtilities.h"

class Process;

int main(int argc, char *argv[]) {
	HelpUtilities::ShowHeader();

	const auto errorExitCode = -1;
	Settings settings;
	CommanLineParser commanLineParser;
	if(!commanLineParser.TryParse(argc, argv, settings))
	{
		HelpUtilities::ShowHelp();
		return errorExitCode;
	}

	std::wcout << "Starting: " << settings.GetCommandLine() << std::endl;	
	std::wcout << "in directory: " << settings.GetWorkingDirectory() << std::endl;

	Process process(settings);
	if(!process.IsCreated())
	{
		return errorExitCode;
	}

	return process.GetExitCode();
}