#pragma once
#include <string>
#include <vector>

class StringUtilities
{
public:
	static std::vector<std::wstring> Split(std::wstring &str, const std::wstring separator);
};

