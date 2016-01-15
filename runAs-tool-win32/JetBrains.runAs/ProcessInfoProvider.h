#pragma once
#include "Result.h"

class ProcessInfoProvider
{	
	bool IsWow64() const;
	bool Is64OS() const;

public:
	ProcessInfoProvider();
	~ProcessInfoProvider();

	Result<bool> IsServiceProcess() const;
	Result<bool> IsUserAnAdministrator() const;
	bool IsSuitableOS() const;
};

