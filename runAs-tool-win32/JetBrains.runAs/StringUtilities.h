#pragma once
#include <string>
#include <list>

class StringUtilities
{
public:
	static list<wstring> Split(const wstring &str, const wstring& separator);
	static wstring Convert(const wstring& str, int(*const converter)(int));
};

