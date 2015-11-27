#pragma once
#include <string>
#include <regex>
class Settings;

class CommanLineParser
{
	const std::regex argRegex = std::regex("\\s*/\\s*(\\w)+\\s*:\\s*(.+)\\s*$");
	const std::regex userRegex = std::regex("^([^@\\\\]+)@([^@\\\\]+)$|^([^@\\\\]+)\\\\([^@\\\\]+)$|(^[^@\\\\]+$)");

	static std::wstring ToWString(const std::string& text);

public:
	CommanLineParser();	
	bool TryParse(int argc, char *argv[], Settings& settings) const;	
};