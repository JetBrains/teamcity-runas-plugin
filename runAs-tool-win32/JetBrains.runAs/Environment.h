#pragma once
class Environment
{
	void* _environment;

public:
	Environment();
	~Environment();

	void* GetEnvironment() const;
};

