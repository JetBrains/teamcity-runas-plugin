#include "IProcess.h"

class Settings;

class ProcessWithLogon : public IProcess
{
public:
	virtual int Run(Settings& settings) const override;
};
