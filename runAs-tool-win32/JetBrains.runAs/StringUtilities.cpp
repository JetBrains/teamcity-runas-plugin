#include "stdafx.h"
#include "StringUtilities.h"
#include <algorithm>

std::vector<std::wstring> StringUtilities::Split(std::wstring &str, const std::wstring separator)
{
	std::vector<std::wstring> strs;
	auto separatorSize = separator.size();
	size_t start = 0;
	size_t pos;
	while ((pos = str.find(separator, start)) != std::wstring::npos)
	{
		strs.push_back(str.substr(start, pos - start - separatorSize));
		start = pos + separatorSize;		
	}

	pos = str.size() - separatorSize;
	if (start < pos)
	{
		strs.push_back(str.substr(start, pos));
	}

	return strs;
}

std::wstring StringUtilities::Convert(std::wstring str, int(* converter)(int))
{
	std::wstring convertedStr;
	convertedStr.resize(str.size());
	transform(str.begin(), str.end(), convertedStr.begin(), converter);
	return convertedStr;
}
