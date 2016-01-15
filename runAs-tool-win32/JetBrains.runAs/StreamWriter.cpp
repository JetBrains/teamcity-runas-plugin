#include "stdafx.h"
#include "StringWriter.h"

StringWriter::StringWriter(std::stringstream& stream): _stream(stream)
{	
}

bool StringWriter::WriteFile(void* buffer, unsigned long size)
{
	_stream << std::string(static_cast<char*>(buffer), size / sizeof(char));
	return true;
}
