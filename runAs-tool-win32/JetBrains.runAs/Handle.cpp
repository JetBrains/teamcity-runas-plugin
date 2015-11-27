#include "stdafx.h"
#include "Handle.h"
#include <iostream>
#include "ErrorUtilities.h"

Handle::Handle(std::wstring name)
{
	_name = name;
}

Handle::~Handle()
{
	if (_handle != nullptr && !CloseHandle(_handle))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(ErrorUtilities::GetActionName(L"CloseHandle", _name));
	}
}

HANDLE& Handle::GetHandle()
{
	return _handle;
}