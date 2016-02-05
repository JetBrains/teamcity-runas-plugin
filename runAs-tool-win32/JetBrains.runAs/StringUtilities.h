#pragma once
#include <string>
#include <vector>

class StringUtilities
{
public:
	static std::vector<std::wstring> Split(std::wstring &str, const std::wstring separator);
	static std::wstring Convert(std::wstring str, int(* converter)(int));
};

