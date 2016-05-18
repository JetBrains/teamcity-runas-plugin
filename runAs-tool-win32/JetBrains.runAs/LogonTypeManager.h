#pragma once
#include "LogonType.h"

class LogonTypeManager
{
public:
	DWORD GetLogonTypeFlag(LogonType logonType) const;
};

