#pragma once
#include <string>

class Handle
{
	std::wstring _name;
	HANDLE _handle = nullptr;	

public:
	explicit Handle(std::wstring name);
	~Handle();	
	operator HANDLE() const;
	Handle& operator = (const HANDLE handle);
	PHANDLE operator &();
	bool IsInvalid() const;
};