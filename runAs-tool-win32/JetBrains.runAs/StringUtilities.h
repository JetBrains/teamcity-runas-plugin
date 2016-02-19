#pragma once
#include <string>
#include <vector>

class StringUtilities
{
public:
	static vector<wstring> Split(wstring &str, const wstring separator);
	static wstring Convert(wstring str, int(* converter)(int));
};

