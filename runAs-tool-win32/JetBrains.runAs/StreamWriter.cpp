#include "stdafx.h"
#include "StreamWriter.h"

StreamWriter::StreamWriter(HANDLE hStream)
{
	_hStream = hStream;
}

bool StreamWriter::WriteFile(void* buffer, unsigned long size)
{
	DWORD bytesWritten;
	return ::WriteFile(_hStream, buffer, size, &bytesWritten, nullptr) == TRUE;
}
