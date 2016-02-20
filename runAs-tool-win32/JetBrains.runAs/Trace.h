#pragma once
#include "LogLevel.h"

class Trace
{
	const LogLevel _logLevel;

public:
	Trace(const LogLevel& logLevel);
	Trace& operator < (const wstring& text);
	Trace& operator << (const wstring& text);
	Trace& operator < (const size_t num);
	Trace& operator << (const size_t num);
};

