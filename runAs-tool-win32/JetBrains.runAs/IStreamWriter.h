#pragma once
class IStreamWriter
{
public:
	virtual ~IStreamWriter() {};

	virtual bool WriteFile(void* buffer, unsigned long size) = 0;
};

