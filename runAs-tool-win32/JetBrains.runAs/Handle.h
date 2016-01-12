#pragma once
#include <string>

class Handle
{
	std::wstring _name;
	HANDLE _handle = nullptr;	

public:
	explicit Handle(std::wstring name);
	~Handle();

	HANDLE& Value();
	bool IsInvalid() const;
};