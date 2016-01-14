#pragma once
#include <string>
class Settings;

class CommanLineParser
{
	static std::wstring ToWString(const std::string& text);

public:
	CommanLineParser();	
	bool TryParse(int argc, _TCHAR *argv[], Settings& settings) const;
};