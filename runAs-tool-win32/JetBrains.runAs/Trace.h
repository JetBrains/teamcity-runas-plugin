#pragma once
#include "LogLevel.h"

class Trace
{
	const LogLevel _logLevel;

public:
	Trace(const LogLevel& logLevel);
	Trace& operator << (const wstring& text);
	Trace& operator << (const int num);
};

