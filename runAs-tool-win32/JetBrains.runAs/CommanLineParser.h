#pragma once
#include <string>
#include "Result.h"
#include <list>
#include "ExitCode.h"
#include "LogLevel.h"
class Settings;

class CommanLineParser
{		
public:
	Result<Settings> TryParse(const list<wstring>& args, ExitCode* exitCodeBase, LogLevel* logLevel) const;
};