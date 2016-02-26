#pragma once
#include <string>
#include <vector>

class StringUtilities
{
public:
	static vector<wstring> Split(const wstring &str, const wstring& separator);
	static wstring Convert(const wstring& str, int(*const converter)(int));
};

