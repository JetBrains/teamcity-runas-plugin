#pragma once
#include <string>
#include "Result.h"
#include <list>
#include "ExitCode.h"
class Settings;

class CommanLineParser
{
	static std::wstring NormalizeCmdArg(std::wstring cmdArg);
	
public:
	CommanLineParser();	
	Result<Settings> TryParse(std::list<std::wstring> args, ExitCode* exitCodeBase) const;
};