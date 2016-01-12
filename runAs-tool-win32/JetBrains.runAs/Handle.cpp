#include "stdafx.h"
#include "Handle.h"
#include <iostream>
#include "ErrorUtilities.h"

Handle::Handle(std::wstring name)
{
	_name = name;
	_handle = INVALID_HANDLE_VALUE;
}

Handle::~Handle()
{
	if (_handle != nullptr && !IsInvalid() && !CloseHandle(_handle))
	{
		std::wcerr << ErrorUtilities::GetLastErrorMessage(ErrorUtilities::GetActionName(L"CloseHandle", _name));
	}
}

HANDLE& Handle::Value()
{
	return _handle;
}

bool Handle::IsInvalid() const
{
	return _handle == INVALID_HANDLE_VALUE;
}
