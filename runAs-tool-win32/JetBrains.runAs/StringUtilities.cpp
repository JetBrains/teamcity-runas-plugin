#include "stdafx.h"
#include "StringUtilities.h"

std::vector<std::wstring> StringUtilities::Split(std::wstring &str, const std::wstring separator)
{
	std::vector<std::wstring> strs;
	size_t i = 0;
	auto pos = str.find(separator);
	while (pos != std::wstring::npos)
	{
		strs.push_back(str.substr(i, pos - i));
		i = ++pos;
		pos = str.find(separator, pos);
		if (pos == std::string::npos)
		{
			strs.push_back(str.substr(i, str.length()));
		}
	}

	return strs;
}
