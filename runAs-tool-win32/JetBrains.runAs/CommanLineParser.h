#pragma once
#include <string>
#include "Result.h"
class Settings;

class CommanLineParser
{
	static std::wstring NormalizeCmdArg(std::wstring cmdArg);
	
public:
	CommanLineParser();	
	Result<bool> TryParse(int argc, _TCHAR *argv[], Settings& settings) const;
};