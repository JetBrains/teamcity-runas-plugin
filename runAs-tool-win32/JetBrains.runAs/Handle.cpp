#include "stdafx.h"
#include "Handle.h"

Handle::Handle(wstring name)
{
	_name = name;
	_handle = INVALID_HANDLE_VALUE;
}

Handle::~Handle()
{
	if (_handle != nullptr && !IsInvalid())
	{
		CloseHandle(_handle);
	}
}

Handle::operator HANDLE() const
{
	return _handle;
}

Handle& Handle::operator=(const HANDLE handle)
{
	_handle = handle;
	return *this;
}

PHANDLE Handle::operator&()
{
	return &_handle;
}

bool Handle::IsInvalid() const
{
	return _handle == INVALID_HANDLE_VALUE;
}
