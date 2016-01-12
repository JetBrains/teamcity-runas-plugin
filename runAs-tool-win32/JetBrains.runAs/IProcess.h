#pragma once

class Settings;

class IProcess
{
public:
	const static int ErrorExitCode = -1;

	virtual ~IProcess()	{}

	virtual int Run(Settings& settings) const = 0;
};