#include "stdafx.h"
#include "Environment.h"
#include "ErrorUtilities.h"
#include <iostream>

Environment::Environment()
{
	if (!CreateEnvironmentBlock(&_environment, nullptr, true))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"CreateEnvironmentBlock");		
	}	
}


Environment::~Environment()
{
	if (!DestroyEnvironmentBlock(_environment))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(L"DestroyEnvironmentBlock");
	}
}

void* Environment::GetEnvironment() const
{
	return _environment;
}
