#include "stdafx.h"
#include "Trace.h"
#include <iostream>

Trace::Trace(const LogLevel& logLevel)
	:_logLevel(logLevel)
{
}

Trace& Trace::operator<(const wstring& text)
{
	if (_logLevel == LOG_LEVEL_DEBUG)
	{
		wcout << endl << text;
	}

	return *this;
}

Trace& Trace::operator<<(const wstring& text)
{
	if (_logLevel == LOG_LEVEL_DEBUG)
	{
		wcout << text;
	}

	return *this;
}

Trace& Trace::operator<(const size_t num)
{
	if (_logLevel == LOG_LEVEL_DEBUG)
	{
		wcout << endl << num;
	}

	return *this;
}

Trace& Trace::operator<<(const size_t num)
{
	if (_logLevel == LOG_LEVEL_DEBUG)
	{
		wcout << num;
	}

	return *this;
}