#include "stdafx.h"
#include "StringUtilities.h"
#include <algorithm>

vector<wstring> StringUtilities::Split(const wstring& str, const wstring& separator)
{
	vector<wstring> strs;
	auto separatorSize = separator.size();
	size_t start = 0;
	size_t pos;
	while ((pos = str.find(separator, start)) != wstring::npos)
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

wstring StringUtilities::Convert(const wstring& str, int(*const converter)(int))
{
	wstring convertedStr;
	convertedStr.resize(str.size());
	transform(str.begin(), str.end(), convertedStr.begin(), converter);
	return convertedStr;
}
